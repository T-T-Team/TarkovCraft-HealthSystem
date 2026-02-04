package tnt.tarkovcraft.medsystem.common.armor;

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
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.apache.commons.lang3.mutable.MutableFloat;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.health.calc.HitInfo;
import tnt.tarkovcraft.medsystem.common.init.MedSystemAttributes;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModularArmorComponent implements ArmorComponent {

    private final float armorValueMultiplier;

    protected ModularArmorComponent(float armorValueMultiplier) {
        this.armorValueMultiplier = armorValueMultiplier;
    }

    @Override
    public void applyItemDamage(ArmorHurtEvent event, DamageContext context) {
        LivingEntity entity = event.getEntity();
        Set<EquipmentSlot> hitSlots = context.getArmorDamageSlots();
        Set<EquipmentSlot> armorSlots = new HashSet<>(event.getArmorMap().keySet());
        Map<EquipmentSlot, ArmorHurtEvent.ArmorEntry> map = event.getArmorMap();
        float damageReductionMultiplier = AttributeSystem.getFloatValue(entity, MedSystemAttributes.ARMOR_DURABILITY, 1.0F);
        for (EquipmentSlot slot : armorSlots) {
            if (!hitSlots.contains(slot)) {
                map.remove(slot);
            } else {
                float damage = event.getNewDamage(slot);
                if (damage > 0 && damageReductionMultiplier != 1.0F) {
                    event.setNewDamage(slot, Math.max(damage * damageReductionMultiplier, 1.0F));
                }
            }
        }
    }

    @Override
    public void applyDamageReduction(LivingIncomingDamageEvent event, DamageContext context) {
        LivingEntity entity = event.getEntity();
        // we work only with first limb as that should be the damage entry, the rest happens "within" the entity itself
        Limb limb = context.getHits().stream().map(HitInfo::limb).findFirst()
                .orElse(null);
        if (limb == null)
            return;

        // calculate reductions
        LimbType type = limb.getType();
        DamageSource source = event.getSource();
        Set<EquipmentSlot> slots = type.getArmorSlots();
        MutableFloat armorValue = new MutableFloat(0.0F);
        MutableFloat enchantmentValue = new MutableFloat(0.0F);
        this.calculateReductions(slots, context, entity, armorValue, enchantmentValue);

        // apply armor reduction
        float toughness = (float) entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float damageAfterAbsorb = CombatRules.getDamageAfterAbsorb(entity, event.getAmount(), context.getSource(), armorValue.floatValue() * this.armorValueMultiplier, toughness);
        float armorReduction = event.getAmount() - damageAfterAbsorb;
        if (armorReduction > 0.0F) {
            event.addReductionModifier(DamageContainer.Reduction.ARMOR, new ForcedReductionFunction(armorReduction));
        }

        // apply enchantment reduction
        float enchantment = source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS) || source.is(DamageTypeTags.BYPASSES_EFFECTS) ? 0.0F : enchantmentValue.floatValue();
        if (enchantment > 0.0F) {
            float enchantAdjustedDamage = CombatRules.getDamageAfterMagicAbsorb(event.getAmount(), enchantment);
            float enchantReduction = event.getAmount() - enchantAdjustedDamage;
            if (enchantReduction > 0.0F) {
                event.addReductionModifier(DamageContainer.Reduction.ENCHANTMENTS, new ForcedReductionFunction(enchantReduction));
            }
        }
    }

    private void calculateReductions(Collection<EquipmentSlot> slots, DamageContext ctx, LivingEntity entity, MutableFloat armor, MutableFloat enchants) {
        double armorAttribute = entity.getAttribute(Attributes.ARMOR).getBaseValue();
        for (EquipmentSlot slot : slots) {
            ItemStack itemStack = entity.getItemBySlot(slot);
            if (itemStack.isEmpty())
                continue;
            ctx.addArmorDamageSlot(slot);
            ItemAttributeModifiers modifiers = itemStack.getAttributeModifiers();
            for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
                if (entry.attribute().is(Attributes.ARMOR)) {
                    AttributeModifier modifier = entry.modifier();
                    if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                        armorAttribute += modifier.amount();
                    }
                }
            }

            EnchantmentHelper.runIterationOnItem(itemStack, slot, entity, (enchantment, enchLevel, enchantedItem) ->
                    enchantment.value().modifyDamageProtection((ServerLevel) entity.level(), enchLevel, enchantedItem.itemStack(), entity, ctx.getSource(), enchants));
        }
        armor.setValue(armorAttribute);
    }
}
