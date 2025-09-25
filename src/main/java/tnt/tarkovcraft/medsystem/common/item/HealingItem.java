package tnt.tarkovcraft.medsystem.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import tnt.tarkovcraft.core.common.skill.SkillSystem;
import tnt.tarkovcraft.core.util.helper.TextHelper;
import tnt.tarkovcraft.medsystem.api.heal.*;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainerDefinition;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.common.init.MedSystemSkillEvents;
import tnt.tarkovcraft.medsystem.network.message.S2C_OpenBodyPartSelectScreen;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class HealingItem extends InteractableItem implements SideEffectProcessor {

    private final ItemUseAnimation useAnimation;

    public HealingItem(ItemUseAnimation animation, Properties properties) {
        super(properties);
        this.useAnimation = animation;
    }

    public HealingItem(Properties properties) {
        this(ItemUseAnimation.BOW, properties);
    }

    @Override
    protected boolean canUseItem(ItemStack itemStack, LivingEntity target, LivingEntity origin) {
        if (!super.canUseItem(itemStack, target, origin)) {
            return false;
        }
        if (!HealthSystem.hasCustomHealth(target)) {
            return false;
        }
        HealItemAttributes attributes = this.getHealingAttributes(itemStack);
        if (attributes == null) {
            return false;
        }
        return attributes.canUseOn(target, itemStack, HealthSystem.getHealthData(target));
    }

    @Override
    protected boolean tryInitiateExistingInteraction(ItemStack itemStack, InteractionTarget interaction, LivingEntity target, Player origin) {
        HealthContainer container = HealthSystem.getHealthData(target);
        HealItemAttributes attributes = this.getHealingAttributes(itemStack);
        if (attributes == null) {
            return false;
        }
        return !origin.isCrouching() && (attributes.applyGlobally() || container.hasBodyPart(interaction.limbCode()));
    }

    @Override
    protected InteractionResult initiateInteraction(ItemStack itemStack, InteractionTarget.Mutable interaction, LivingEntity target, Player origin) {
        HealItemAttributes attributes = this.getHealingAttributes(itemStack);
        if (!attributes.applyGlobally()) {
            Level level = origin.level();
            // TODO auto-select body part if player is not crouched
            if (!level.isClientSide()) {
                PacketDistributor.sendToPlayer((ServerPlayer) origin, new S2C_OpenBodyPartSelectScreen(interaction));
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean updateInteraction(Level level, ItemStack itemStack, InteractionTarget interaction, LivingEntity target, LivingEntity origin, int remainingUseTicks) {
        HealItemAttributes attributes = this.getHealingAttributes(itemStack);
        if (attributes == null) {
            return false;
        }
        HealthRecovery healthRecovery = attributes.health();
        if (healthRecovery == null)
            return true;

        int useDuration = this.getUseDuration(itemStack, origin);
        int usageTimeElapsed = useDuration - remainingUseTicks + 1;
        if (usageTimeElapsed % healthRecovery.cycleDuration() == 0) {
            int cycleLimit = healthRecovery.maxCycles() == 0 ? Integer.MAX_VALUE : healthRecovery.cycleDuration();
            int cycleIndex = usageTimeElapsed / healthRecovery.cycleDuration();
            if (cycleIndex < cycleLimit) {
                float amount = healthRecovery.healthPerCycle();
                HealthContainer container = HealthSystem.getHealthData(target);
                InteractionTarget activeInteraction = this.getActiveInteraction(itemStack);
                BodyPart part = activeInteraction != null && TextHelper.isNotBlank(activeInteraction.limbCode()) && container.hasBodyPart(activeInteraction.limbCode())
                        ? container.getBodyPart(activeInteraction.limbCode())
                        : null;

                if (level instanceof ServerLevel serverLevel) {
                    SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.HEALING_USED, origin);
                    if (itemStack.isDamageableItem()) {
                        itemStack.hurtAndBreak(1, serverLevel, origin, item -> origin.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
                    } else {
                        itemStack.consume(1, origin);
                    }
                }
                float leftover = container.heal(target, amount, part);
                if (leftover == amount) {
                    origin.useItemRemaining = 0;
                }
                if (leftover > 0 && container.canHeal(null, false)) {
                    container.heal(target, amount, null);
                }
                container.updateHealth(target);
                if (cycleIndex + 1 > cycleLimit || (part != null && !attributes.canUseOnPart(part, itemStack, container))) {
                    origin.useItemRemaining = 0;
                } else {
                    HealthSystem.synchronizeEntity(target);
                }
            }
        }

        return true;
    }

    @Override
    protected ItemStack finishInteraction(ItemStack itemStack, InteractionTarget interaction, LivingEntity target, LivingEntity origin) {
        HealItemAttributes attributes = this.getHealingAttributes(itemStack);
        String targetLimb = interaction.limbCode();
        if (!attributes.applyGlobally() && TextHelper.isBlank(interaction.limbCode())) {
            return itemStack;
        }

        HealthContainer container = HealthSystem.getHealthData(target);
        BodyPart part = container.hasBodyPart(targetLimb) ? container.getBodyPart(targetLimb) : null;
        int consume = 0;
        // dead limb recovery
        if (attributes.isSurgeryItem()) {
            Surgery surgery = attributes.surgery();
            consume++; // dead limb fix has hardcoded consumption value of 1
            if (part.isDead()) {
                SkillSystem.trigger(MedSystemSkillEvents.LIMB_FIXED, origin); // reward the healer
                part.setHealth(surgery.healthAfterHeal());
                surgery.addRecoveryAttributes(target, part);
            }
        }
        // effect recovery + consumption for recovery
        List<EffectRecovery> recoveries = attributes.recoveries();
        for (EffectRecovery recovery : recoveries) {
            if (recovery.canRecover(container, part) && checkDurability(itemStack, consume + recovery.consumption())) {
                recovery.recover(target, container, part);
                consume += recovery.consumption();
            }
        }
        // Side effect application
        if (itemStack.has(MedSystemItemComponents.SIDE_EFFECTS)) {
            SideEffectHolder holder = itemStack.get(MedSystemItemComponents.SIDE_EFFECTS);
            holder.apply(target, container, part);
        }
        // Consume effect application
        Level level = origin.level();
        List<ConsumeEffect> consumeEffects = attributes.effects();
        for (ConsumeEffect effect : consumeEffects) {
            effect.apply(level, itemStack, target);
        }
        // Apply durability reduction
        if (!level.isClientSide()) {
            int consumeAmount = Math.max(1, consume);
            SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.HEALING_USED, origin, consumeAmount);
            if (itemStack.isDamageableItem()) {
                itemStack.hurtAndBreak(consumeAmount, (ServerLevel) level, origin, item -> origin.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
            } else {
                itemStack.consume(1, origin);
            }
        }
        container.updateHealth(target);
        HealthSystem.synchronizeEntity(target);
        return itemStack;
    }

    @Override
    protected boolean canInteractWithEntity(ItemStack stack, LivingEntity entity, LivingEntity origin) {
        HealthContainer container = HealthSystem.getHealthData(entity);
        HealthContainerDefinition definition = container.getDefinition();
        return !definition.getDisplayConfiguration().isEmpty();
    }

    @Override
    protected boolean shouldClearInteractionDataOnCancellation(ItemStack itemStack, LivingEntity entity, int count) {
        InteractionTarget interaction = this.getActiveInteraction(itemStack);
        return interaction != null && !interaction.self();
    }

    @Override
    protected @Nullable Component getInteractionLabel(InteractionTarget interaction, LivingEntity target, LivingEntity origin, int time, boolean infinite) {
        if (interaction.self()) {
            return infinite
                    ? Component.translatable("label.medsystem.healing.self.infinite")
                    : Component.translatable("label.medsystem.healing.self", String.format(Locale.ROOT, "%.2f", time / 20.0F));
        } else {
            return infinite
                    ? Component.translatable("label.medsystem.healing.other.infinite", target.getDisplayName())
                    : Component.translatable("label.medsystem.healing.other", target.getDisplayName(), String.format(Locale.ROOT, "%.2f", time / 20.0F));
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        HealItemAttributes attributes = stack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
        return attributes.getUseDuration(APPROXIMATELY_INFINITE_USE_DURATION);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return this.useAnimation;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return false;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF0000;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        int max = Math.max(stack.getMaxDamage(), 1);
        int damage = max - stack.getDamageValue();
        Component durability = Component.literal(damage + "/" + max).withStyle(ChatFormatting.RED);
        tooltipAdder.accept(Component.translatable("tooltip.medsystem.item.durability", durability).withStyle(ChatFormatting.GRAY));
    }

    public static boolean checkDurability(ItemStack stack, int durabilityUse) {
        int maxDamage = Math.max(stack.getMaxDamage(), 1) - stack.getDamageValue();
        return durabilityUse <= maxDamage;
    }

    private HealItemAttributes getHealingAttributes(ItemStack itemStack) {
        return itemStack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
    }
}
