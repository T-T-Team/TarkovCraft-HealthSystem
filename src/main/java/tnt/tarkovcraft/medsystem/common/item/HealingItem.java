package tnt.tarkovcraft.medsystem.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.network.PacketDistributor;
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
        if (healTarget != null) {
            if (healTarget.self()) {
                this.tickHealingOn(healTarget, livingEntity, livingEntity, level, stack, remainingUseDuration);
            } else {
                Entity entity = level.getEntity(healTarget.entityId());
                if (!(entity instanceof LivingEntity targetEntity)) {
                    livingEntity.stopUsingItem();
                } else {
                    this.tickHealingOn(healTarget, targetEntity, livingEntity, level, stack, remainingUseDuration);
                }
            }
        }
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        stack.remove(MedSystemItemComponents.HEAL_TARGET);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        HealTarget target = this.getSelectedHealingTarget(stack);
        if (target.self()) {
            return this.finishUsingItemOn(target, livingEntity, livingEntity, level, stack);
        } else {
            Entity entity = level.getEntity(target.entityId());
            if (entity instanceof LivingEntity targetEntity) {
                return this.finishUsingItemOn(target, targetEntity, livingEntity, level, stack);
            }
        }
        return stack;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HealTarget healTarget = this.getSelectedHealingTarget(stack);
        if (healTarget != null) {
            LivingEntity target = healTarget.getTargetLivingEntity(player);
            HealthContainer existingTargetHealth = HealthSystem.getHealthData(target);
            if (this.canUseItem(stack, target, player)) {
                HealItemAttributes attributes = stack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
                if (attributes.applyGlobally() || existingTargetHealth.hasBodyPart(healTarget.limbCode())) {
                    player.startUsingItem(hand);
                    return InteractionResult.SUCCESS;
                }

            }
        }
        LivingEntity livingEntity = this.identifyPossibleHealingTarget(stack, player, level);
        if (livingEntity == null) {
            stack.remove(MedSystemItemComponents.HEAL_TARGET);
            return InteractionResult.FAIL;
        }
        boolean selfHealing = player == livingEntity;
        HealItemAttributes attributes = stack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
        // TODO auto-identify most critically damaged limb
        HealTarget target = new HealTarget(selfHealing, selfHealing ? 0 : livingEntity.getId(), "");
        if ((player.isCrouching() && !attributes.applyGlobally()) || !attributes.applyGlobally()) {
            if (!level.isClientSide()) {
                PacketDistributor.sendToPlayer((ServerPlayer) player, new S2C_OpenBodyPartSelectScreen(target.self(), target.entityId()));
            }
            return InteractionResult.CONSUME;
        } else {
            stack.set(MedSystemItemComponents.HEAL_TARGET, target);
            player.startUsingItem(hand);
            return InteractionResult.SUCCESS;
        }
    }

    private LivingEntity identifyPossibleHealingTarget(ItemStack stack, LivingEntity healer, Level level) {
        double range = 3.0;
        Vec3 eye = healer.getEyePosition();
        Vec3 look = healer.getViewVector(1.0F);
        Vec3 end = eye.add(look.x * range, look.y * range, look.z * range);
        AABB aabb = healer.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0, 1.0, 1.0);
        HitResult result = ProjectileUtil.getEntityHitResult(healer, eye, end, aabb, EntitySelector.LIVING_ENTITY_STILL_ALIVE, range * range);
        LivingEntity entity = null;
        if (result != null) {
            result = filterHitResult(result, eye, range);
        }
        if (result != null && result.getType() == HitResult.Type.ENTITY) {
            entity = (LivingEntity) ((EntityHitResult) result).getEntity();
        }
        if (entity != null && HealthSystem.hasCustomHealth(entity) && this.canUseItem(stack, entity, healer)) {
            HealthContainer container = HealthSystem.getHealthData(entity);
            HealthContainerDefinition definition = container.getDefinition();
            if (!definition.getDisplayConfiguration().isEmpty()) {
                return entity;
            }
        }
        if (this.canUseItem(stack, healer, healer)) {
            return healer;
        }
        return null;
    }

    private static HitResult filterHitResult(HitResult hitResult, Vec3 pos, double blockInteractionRange) {
        Vec3 vec3 = hitResult.getLocation();
        if (!vec3.closerThan(pos, blockInteractionRange)) {
            Vec3 vec31 = hitResult.getLocation();
            Direction direction = Direction.getApproximateNearest(vec31.x - pos.x, vec31.y - pos.y, vec31.z - pos.z);
            return BlockHitResult.miss(vec31, direction, BlockPos.containing(vec31));
        } else {
            return hitResult;
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

    public final HealTarget getSelectedHealingTarget(ItemStack stack) {
        return stack.get(MedSystemItemComponents.HEAL_TARGET);
    }

    public boolean canUseItem(ItemStack stack, LivingEntity entity, LivingEntity healer) {
        if (!HealthSystem.hasCustomHealth(entity)) {
            return false;
        }
        if (!stack.has(MedSystemItemComponents.HEAL_ATTRIBUTES)) {
            return false;
        }
        if (healer != entity && healer.distanceToSqr(entity) > 10) {
            return false;
        }
        if (entity instanceof Player player && player.getCooldowns().isOnCooldown(stack)) {
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
        if (!this.canUseItem(itemStack, targetEntity, healer)) {
            itemStack.remove(MedSystemItemComponents.HEAL_TARGET);
            healer.stopUsingItem();
            if (healer instanceof Player player) {
                ItemCooldowns cooldowns = player.getCooldowns();
                cooldowns.addCooldown(itemStack, 10);
            }
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
            Component message = finite
                    ? Component.translatable("label.medsystem.healing.other", targetEntity.getDisplayName(), String.format(Locale.ROOT, "%.2f", duration / 20.0F))
                    : Component.translatable("label.medsystem.healing.other.infinite", targetEntity.getDisplayName());
            if (!level.isClientSide() && healer instanceof Player player)
                player.displayClientMessage(message, true);
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
        if (!this.canUseItem(stack, targetEntity, healer) || (!attributes.applyGlobally() && TextHelper.isBlank(targetLimb))) {
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
                recovery.recover(targetEntity, container, part);
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
