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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import tnt.tarkovcraft.core.common.Notification;
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
        if (!this.canUseItem(stack, livingEntity)) {
            stack.remove(MedSystemItemComponents.SELECTED_BODY_PART);
            livingEntity.stopUsingItem();
            return;
        }
        HealItemAttributes attributes = stack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
        HealthRecovery healthRecovery = attributes.health();
        if (healthRecovery == null)
            return;
        int usageTimeElapsed = attributes.getUseDuration(APPROXIMATELY_INFINITE_USE_DURATION) - remainingUseDuration + 1;
        if (usageTimeElapsed % healthRecovery.cycleDuration() == 0) {
            int cycleLimit = healthRecovery.maxCycles() == 0 ? Integer.MAX_VALUE : healthRecovery.cycleDuration();
            int cycleIndex = usageTimeElapsed / healthRecovery.cycleDuration();
            if (cycleIndex <= cycleLimit) {
                float amount = healthRecovery.healthPerCycle();
                HealthContainer container = HealthSystem.getHealthData(livingEntity);
                String partId = this.getSelectedBodyPart(stack);
                BodyPart part = TextHelper.isNotBlank(partId) && container.hasBodyPart(partId) ? container.getBodyPart(partId) : null;
                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                    SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.HEALING_USED, livingEntity);
                    if (stack.isDamageableItem()) {
                        stack.hurtAndBreak(1, serverLevel, livingEntity, item -> livingEntity.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
                    } else {
                        stack.consume(1, livingEntity);
                    }
                }
                float leftover = container.heal(livingEntity, amount, part);
                if (leftover == amount) {
                    livingEntity.useItemRemaining = 0;
                }
                if (leftover > 0 && container.canHeal(null, false)) {
                    container.heal(livingEntity, amount, null);
                }
                container.updateHealth(livingEntity);
                if (cycleIndex + 1 > cycleLimit) {
                    livingEntity.useItemRemaining = 0;
                } else {
                    HealthSystem.synchronizeEntity(livingEntity);
                }
            }
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        String targetLimb = this.getTargetLimb(stack);
        HealItemAttributes attributes = stack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
        if (!this.canUseItem(stack, livingEntity) || (!attributes.applyGlobally() && TextHelper.isBlank(targetLimb))) {
            if (livingEntity instanceof ServerPlayer player) {
                Notification notification = Notification.error(Component.literal("Unable to use item, target limb not found"));
                notification.send(player);
            }
            return stack;
        }

        HealthContainer container = HealthSystem.getHealthData(livingEntity);
        BodyPart part = TextHelper.isNotBlank(targetLimb) && container.hasBodyPart(targetLimb) ? container.getBodyPart(targetLimb) : null;
        int consume = 0;
        // dead limb recovery
        if (attributes.canHealDeadLimbs()) {
            DeadLimbHealing deadLimbHealing = attributes.deadLimbHealing();
            consume++; // dead limb fix has hardcoded consumption value of 1
            if (part.isDead()) {
                SkillSystem.trigger(MedSystemSkillEvents.LIMB_FIXED, livingEntity);
                part.setHealth(deadLimbHealing.healthAfterHeal());
                deadLimbHealing.addRecoveryAttributes(livingEntity, part);
            }
        }
        // effect recovery + consumption for recovery
        List<EffectRecovery> recoveries = attributes.recoveries();
        for (EffectRecovery recovery : recoveries) {
            if (recovery.canRecover(container, part) && checkDurability(stack, consume + recovery.consumption())) {
                recovery.recover(livingEntity, container, stack, part);
                consume += recovery.consumption();
            }
        }
        // Side effect application
        if (stack.has(MedSystemItemComponents.SIDE_EFFECTS)) {
            SideEffectHolder holder = stack.get(MedSystemItemComponents.SIDE_EFFECTS);
            holder.apply(livingEntity, container, part);
        }
        // Apply durability reduction
        if (!level.isClientSide()) {
            int consumeAmount = Math.max(1, consume);
            SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.HEALING_USED, livingEntity, consumeAmount);
            if (stack.isDamageableItem()) {
                stack.hurtAndBreak(consumeAmount, (ServerLevel) level, livingEntity, item -> livingEntity.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
            } else {
                stack.consume(1, livingEntity);
            }
        }
        // Remove saved body part and sync data
        stack.remove(MedSystemItemComponents.SELECTED_BODY_PART);
        container.updateHealth(livingEntity);
        HealthSystem.synchronizeEntity(livingEntity);
        if (livingEntity instanceof Player player) {
            ItemCooldowns cooldowns = player.getCooldowns();
            cooldowns.addCooldown(stack, 10);
        }
        return stack;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.canUseItem(stack, player)) {
            String selectedBodyPart = this.getSelectedBodyPart(stack);
            HealItemAttributes attributes = stack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
            if (attributes.applyGlobally() || (!player.isCrouching() && selectedBodyPart != null && player.getData(MedSystemDataAttachments.HEALTH_CONTAINER).hasBodyPart(selectedBodyPart))) {
                player.startUsingItem(hand);
                return InteractionResult.SUCCESS;
            } else {
                if (!level.isClientSide()) {
                    PacketDistributor.sendToPlayer((ServerPlayer) player, new S2C_OpenBodyPartSelectScreen());
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

    public final String getSelectedBodyPart(ItemStack stack) {
        return stack.get(MedSystemItemComponents.SELECTED_BODY_PART);
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

    protected String getTargetLimb(ItemStack stack) {
        return stack.get(MedSystemItemComponents.SELECTED_BODY_PART);
    }

    public static boolean checkDurability(ItemStack stack, int durabilityUse) {
        int maxDamage = Math.max(stack.getMaxDamage(), 1) - stack.getDamageValue();
        return durabilityUse <= maxDamage;
    }
}
