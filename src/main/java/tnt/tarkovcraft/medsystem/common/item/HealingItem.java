package tnt.tarkovcraft.medsystem.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import tnt.tarkovcraft.core.common.skill.SkillSystem;
import tnt.tarkovcraft.core.util.helper.TextHelper;
import tnt.tarkovcraft.medsystem.api.heal.*;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.common.init.MedSystemSkillEvents;
import tnt.tarkovcraft.medsystem.network.message.S2C_OpenBodyPartSelectScreen;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class HealingItem extends Item implements SideEffectProcessor {

    private final ItemUseAnimation useAnimation;

    public HealingItem(ItemUseAnimation animation, Properties properties) {
        super(properties);
        this.useAnimation = animation;
    }

    public HealingItem(Properties properties) {
        this(ItemUseAnimation.BOW, properties);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        HealTarget healTarget = this.getSelectedHealingTarget(stack);
        if (healTarget.self()) {
            this.tickHealingOn(healTarget, livingEntity, livingEntity, level, stack, remainingUseDuration);
        } else {
            // TODO implement later
            livingEntity.stopUsingItem();
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        HealTarget target = this.getSelectedHealingTarget(stack);
        if (target.self()) {
            return this.finishUsingItemOn(target, livingEntity, livingEntity, level, stack);
        }
        // TODO implement other entity healing
        return stack;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // TODO proper healing target select
        if (this.canUseItem(stack, player)) {
            HealTarget target = this.getSelectedHealingTarget(stack);
            HealItemAttributes attributes = stack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
            if (attributes.applyGlobally()) {
                stack.set(MedSystemItemComponents.HEAL_TARGET, new HealTarget(true, 0, ""));
                player.startUsingItem(hand);
            } else if (!player.isCrouching() && target != null && player.getData(MedSystemDataAttachments.HEALTH_CONTAINER).hasBodyPart(target.limbCode())) {
                player.startUsingItem(hand);
                return InteractionResult.SUCCESS;
            } else {
                if (!level.isClientSide()) {
                    PacketDistributor.sendToPlayer((ServerPlayer) player, new S2C_OpenBodyPartSelectScreen(true, 0));
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
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

    public final HealTarget getSelectedHealingTarget(ItemStack stack) {
        return stack.get(MedSystemItemComponents.HEAL_TARGET);
    }

    public boolean canUseItem(ItemStack stack, LivingEntity entity) {
        if (!HealthSystem.hasCustomHealth(entity)) {
            return false;
        }
        if (!stack.has(MedSystemItemComponents.HEAL_ATTRIBUTES)) {
            return false;
        }
        HealItemAttributes attributes = stack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
        return attributes.canUseOn(entity, stack, HealthSystem.getHealthData(entity));
    }

    public static boolean checkDurability(ItemStack stack, int durabilityUse) {
        int maxDamage = Math.max(stack.getMaxDamage(), 1) - stack.getDamageValue();
        return durabilityUse <= maxDamage;
    }

    private void tickHealingOn(HealTarget targetAttributes, LivingEntity targetEntity, LivingEntity healer, Level level, ItemStack itemStack, int duration) {
        if (!this.canUseItem(itemStack, targetEntity)) {
            itemStack.remove(MedSystemItemComponents.HEAL_TARGET);
            healer.stopUsingItem();
            return;
        }

        HealItemAttributes attributes = itemStack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
        if (attributes == null) {
            healer.stopUsingItem();
            return;
        }
        int useDuration = attributes.getUseDuration(APPROXIMATELY_INFINITE_USE_DURATION);
        boolean finite = useDuration < APPROXIMATELY_INFINITE_USE_DURATION;
        if (targetAttributes.self()) {
            Component message = finite
                    ? Component.translatable("label.medsystem.healing.self", String.format(Locale.ROOT, "%.2f", duration / 20.0F))
                    : Component.translatable("label.medsystem.healing.self.infinite");
            if (!level.isClientSide() && healer instanceof Player player)
                player.displayClientMessage(message, true);
        } else {
            // TODO range check
            // TODO message
        }

        HealthRecovery healthRecovery = attributes.health();
        if (healthRecovery == null)
            return;
        int usageTimeElapsed = useDuration - duration + 1;
        if (usageTimeElapsed % healthRecovery.cycleDuration() == 0) {
            int cycleLimit = healthRecovery.maxCycles() == 0 ? Integer.MAX_VALUE : healthRecovery.cycleDuration();
            int cycleIndex = usageTimeElapsed / healthRecovery.cycleDuration();
            if (cycleIndex < cycleLimit) {
                float amount = healthRecovery.healthPerCycle();
                HealthContainer container = HealthSystem.getHealthData(targetEntity);
                HealTarget target = this.getSelectedHealingTarget(itemStack);
                BodyPart part = target != null && TextHelper.isNotBlank(target.limbCode()) && container.hasBodyPart(target.limbCode()) ? container.getBodyPart(target.limbCode()) : null;
                if (level instanceof ServerLevel serverLevel) {
                    SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.HEALING_USED, healer);
                    if (itemStack.isDamageableItem()) {
                        itemStack.hurtAndBreak(1, serverLevel, healer, item -> healer.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
                    } else {
                        itemStack.consume(1, healer);
                    }
                }
                float leftover = container.heal(targetEntity, amount, part);
                if (leftover == amount) {
                    healer.useItemRemaining = 0;
                }
                if (leftover > 0 && container.canHeal(null, false)) {
                    container.heal(targetEntity, amount, null);
                }
                container.updateHealth(targetEntity);
                if (cycleIndex + 1 > cycleLimit) {
                    healer.useItemRemaining = 0;
                } else {
                    HealthSystem.synchronizeEntity(targetEntity);
                }
            }
        }
    }

    private ItemStack finishUsingItemOn(HealTarget targetAttributes, LivingEntity targetEntity, LivingEntity healer, Level level, ItemStack stack) {
        HealItemAttributes attributes = stack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
        String targetLimb = targetAttributes.limbCode();
        if (!this.canUseItem(stack, targetEntity) || (!attributes.applyGlobally() && TextHelper.isBlank(targetLimb))) {
            return stack;
        }

        HealthContainer container = HealthSystem.getHealthData(targetEntity);
        BodyPart part = TextHelper.isNotBlank(targetLimb) && container.hasBodyPart(targetLimb) ? container.getBodyPart(targetLimb) : null;
        int consume = 0;
        // dead limb recovery
        if (attributes.isSurgeryItem()) {
            Surgery surgery = attributes.surgery();
            consume++; // dead limb fix has hardcoded consumption value of 1
            if (part.isDead()) {
                SkillSystem.trigger(MedSystemSkillEvents.LIMB_FIXED, healer); // reward the healer
                part.setHealth(surgery.healthAfterHeal());
                surgery.addRecoveryAttributes(targetEntity, part);
            }
        }
        // effect recovery + consumption for recovery
        List<EffectRecovery> recoveries = attributes.recoveries();
        for (EffectRecovery recovery : recoveries) {
            if (recovery.canRecover(container, part) && checkDurability(stack, consume + recovery.consumption())) {
                recovery.recover(targetEntity, container, stack, part);
                consume += recovery.consumption();
            }
        }
        // Side effect application
        if (stack.has(MedSystemItemComponents.SIDE_EFFECTS)) {
            SideEffectHolder holder = stack.get(MedSystemItemComponents.SIDE_EFFECTS);
            holder.apply(targetEntity, container, part);
        }
        // Consume effect application
        List<ConsumeEffect> consumeEffects = attributes.effects();
        for (ConsumeEffect effect : consumeEffects) {
            effect.apply(level, stack, targetEntity);
        }
        // Apply durability reduction
        if (!level.isClientSide()) {
            int consumeAmount = Math.max(1, consume);
            SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.HEALING_USED, healer, consumeAmount);
            if (stack.isDamageableItem()) {
                stack.hurtAndBreak(consumeAmount, (ServerLevel) level, healer, item -> healer.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
            } else {
                stack.consume(1, healer);
            }
        }
        // Remove saved body part and sync data
        stack.remove(MedSystemItemComponents.HEAL_TARGET);
        container.updateHealth(targetEntity);
        HealthSystem.synchronizeEntity(targetEntity);
        if (healer instanceof Player player) {
            ItemCooldowns cooldowns = player.getCooldowns();
            cooldowns.addCooldown(stack, 10);
        }
        return stack;
    }
}
