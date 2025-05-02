package tnt.tarkovcraft.medsystem.common.health;

import dev.toma.configuration.config.validate.IValidationResult;
import dev.toma.configuration.config.value.IConfigValueReadable;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.damagesource.IReductionFunction;
import org.apache.commons.lang3.mutable.MutableFloat;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.ArmorComponent;
import tnt.tarkovcraft.medsystem.api.ArmorStat;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;

import java.util.Collection;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class DefaultArmorComponent implements ArmorComponent {

    public static final DefaultArmorComponent INSTANCE = new DefaultArmorComponent();

    protected DefaultArmorComponent() {
    }

    @Override
    public boolean useVanillaArmorDamage() {
        return MedicalSystem.getConfig().simpleArmorCalculation;
    }

    @Override
    public void collectAffectedBodyPartsWithProtection(Consumer<BodyPartGroup> register, LivingEntity entity, DamageContext context) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor())
                continue;
            ItemStack itemStack = entity.getItemBySlot(slot);
            if (itemStack.isEmpty())
                continue;

            this.getItemProtectedGroups(itemStack, slot).forEach(register);
        }
    }

    @Override
    public float handleReductions(LivingEntity entity, DamageContext ctx, Set<EquipmentSlot> protectedSlots, Supplier<Float> incomingDamage, FloatConsumer damageProvider, BiConsumer<DamageContainer.Reduction, IReductionFunction> reductionProvider) {
        float armorReduction = this.calculateArmorReduction(entity, ctx, protectedSlots, incomingDamage.get());
        if (armorReduction > 0.0F) {
            reductionProvider.accept(DamageContainer.Reduction.ARMOR, new SetReductionFunction(armorReduction));
        }
        float enchantReduction = this.calculateEnchantReduction(entity, ctx, protectedSlots, incomingDamage.get());
        if (enchantReduction >= 0.0F) {
            reductionProvider.accept(DamageContainer.Reduction.ENCHANTMENTS, new SetReductionFunction(enchantReduction));
        }
        return armorReduction;
    }

    protected float calculateArmorReduction(LivingEntity entity, DamageContext ctx, Set<EquipmentSlot> protectedSlots, float incomingDamage) {
        DamageSource source = ctx.getSource();
        if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            MedSystemConfig config = MedicalSystem.getConfig();
            float armor = config.simpleArmorCalculation ? entity.getArmorValue() : this.calculateArmor(entity, ctx, protectedSlots);
            float armorToughness = (float) entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
            float damageAfterArmorAbsorb = CombatRules.getDamageAfterAbsorb(entity, incomingDamage, ctx.getSource(), armor, armorToughness);
            return incomingDamage - damageAfterArmorAbsorb;
        }
        return 0.0F;
    }

    protected float calculateEnchantReduction(LivingEntity entity, DamageContext ctx, Set<EquipmentSlot> protectedSlots, float incomingDamage) {
        MedSystemConfig config = MedicalSystem.getConfig();
        DamageSource source = ctx.getSource();
        if (config.simpleArmorCalculation || source.is(DamageTypeTags.BYPASSES_EFFECTS) || source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            return -1;
        }
        Level level = entity.level();
        float enchantProtection = level instanceof ServerLevel serverLevel ? this.calculateEnchantReduction(entity, ctx.getSource(), serverLevel, protectedSlots) : 0.0F;
        float adjustedDamage = CombatRules.getDamageAfterMagicAbsorb(incomingDamage, enchantProtection);
        return incomingDamage - adjustedDamage;
    }

    protected Collection<BodyPartGroup> getItemProtectedGroups(ItemStack itemStack, EquipmentSlot slot) {
        if (itemStack.has(MedSystemItemComponents.ARMOR_STAT)) {
            ArmorStat stat = itemStack.get(MedSystemItemComponents.ARMOR_STAT);
            return stat.protectedArea();
        } else {
            return BodyPartGroup.getProtectedByEquipment(slot);
        }
    }

    protected float calculateEnchantReduction(LivingEntity entity, DamageSource source, ServerLevel level, Set<EquipmentSlot> slots) {
        MutableFloat mutableFloat = new MutableFloat(0.0F);
        for (EquipmentSlot slot : slots) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty())
                continue;

            EnchantmentHelper.runIterationOnItem(stack, slot, entity, (enchantment, enchLevel, enchantedItem) ->
                    enchantment.value().modifyDamageProtection(level, enchLevel, enchantedItem.itemStack(), entity, source, mutableFloat));
        }
        return mutableFloat.floatValue();
    }

    protected float calculateArmor(LivingEntity entity, DamageContext context, Collection<EquipmentSlot> protectedSlots) {
        double result = entity.getAttribute(Attributes.ARMOR).getBaseValue();
        for (EquipmentSlot slot : protectedSlots) {
            ItemStack itemStack = entity.getItemBySlot(slot);
            if (itemStack.isEmpty())
                continue;
            context.getAffectedSlots().add(slot);
            ItemAttributeModifiers modifiers = itemStack.getAttributeModifiers();
            for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
                if (entry.attribute().is(Attributes.ARMOR)) {
                    AttributeModifier modifier = entry.modifier();
                    if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                        result += modifier.amount();
                    }
                }
            }
        }
        return (float) result;
    }

    public static IValidationResult checkInUse(boolean simpleArmorCalculation, IConfigValueReadable<Boolean> value) {
        if (simpleArmorCalculation && !HealthSystem.ARMOR.isVanilla()) {
            return IValidationResult.warning(Component.translatable("label.medsystem.validation.config.simpleArmorOverride"));
        }
        return IValidationResult.success();
    }

    protected record SetReductionFunction(float amount) implements IReductionFunction {
        @Override
        public float modify(DamageContainer container, float reductionIn) {
            return this.amount();
        }
    }
}
