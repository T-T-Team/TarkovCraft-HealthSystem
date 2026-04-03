package tnt.tarkovcraft.medsystem.common;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TriState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import tnt.tarkovcraft.core.api.event.EntityWeightUpdateEvent;
import tnt.tarkovcraft.core.api.event.StaminaEvent;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.effect.OverweightStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventParams;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.*;
import tnt.tarkovcraft.medsystem.common.init.*;
import tnt.tarkovcraft.medsystem.common.item.InteractionTarget;
import tnt.tarkovcraft.medsystem.util.HealthHelper;

import java.util.List;
import java.util.function.Consumer;

public final class MedicalSystemEventHandler {

    @SubscribeEvent
    private void onEntitySpawn(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.isCanceled())
            return;
        if (entity instanceof LivingEntity livingEntity) {
            // modular health system
            HealthSystem.handleNewEntity(livingEntity);
            // blood system
            BloodSystemManager.handleNewEntity(livingEntity);
            // damage system
            livingEntity.setData(MedSystemDataAttachments.DAMAGE_CONTEXT, DamageContext.createEmptyInstance());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    private void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        float amount = event.getAmount();
        if (event.isCanceled())
            return;
        if (amount > 0.0F && HealthSystem.hasCustomHealth(entity)) {
            HealthContainer container = HealthContainer.getAttachedValid(entity);
            LimbContainer limbContainer = container.getLimbContainer();
            float leftover = limbContainer.heal(amount, null);
            if (leftover > 0.0F) {
                event.setAmount(amount - leftover);
            }
            HealthSystem.synchronizeEntity(entity);
        }
    }

    @SubscribeEvent
    private void onWeightUpdate(EntityWeightUpdateEvent event) {
        LivingEntity entity = event.getEntity();
        float factor = event.getOverweightFactor();
        HealthContainer container = HealthContainer.getAttachedValid(entity);
        if (container == null)
            return;
        StatusEffectMap effects = container.getGlobalStatusEffects();
        if (factor > 0.0F) {
            effects.replace(new OverweightStatusEffect(factor >= 1.0F));
        } else {
            StatusEffectContext context = StatusEffectContext.of(container, entity, StatusEffectSubmitter.NOOP, null);
            effects.remove(MedSystemStatusEffects.OVERWEIGHT, context);
        }
        HealthSystem.synchronizeEntity(entity);
    }

    @SubscribeEvent
    private void canSprint(StaminaEvent.CanSprint event) {
        LivingEntity entity = event.getEntity();
        MedSystemConfig config = MedicalSystem.getConfig();
        if (config.statusEffects.enableStatusEffects && HealthSystem.isMovementRestricted(entity) && !HealthSystem.hasPainRelief(entity)) {
            event.setCanSprint(false);
        }
    }

    @SubscribeEvent
    private void onSprinted(StaminaEvent.AfterSprint event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        MedSystemConfig config = MedicalSystem.getConfig();
        long gameTime = level.getGameTime();
        if (level.isClientSide())
            return;
        if (config.statusEffects.enableStatusEffects && gameTime % 20L == 0L && HealthSystem.isMovementRestricted(entity)) {
            RegistryAccess access = entity.registryAccess();
            entity.hurtServer((ServerLevel) level, MedSystemDamageTypes.causeFractureDamage(access), 0.25F);
        }
    }

    @SubscribeEvent
    private void afterJump(StaminaEvent.AfterJump event) {
        LivingEntity entity = event.getEntity();
        MedSystemConfig config = MedicalSystem.getConfig();
        Level level = entity.level();
        if (level.isClientSide())
            return;
        if (config.statusEffects.enableStatusEffects && HealthSystem.isMovementRestricted(entity)) {
            RegistryAccess access = entity.registryAccess();
            entity.hurtServer((ServerLevel) level, MedSystemDamageTypes.causeFractureDamage(access), 0.50F);
        }
    }

    @SubscribeEvent
    private void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled())
            return;
        LivingEntity entity = event.getEntity();
        MedSystemConfig config = MedicalSystem.getConfig();
        DamageSource source = event.getSource();
        if (!event.isCanceled() && HealthSystem.hasCustomHealth(entity) && BloodSystemManager.isEnabled(entity) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(entity);
            UnconsciousOptions options = bloodSystem.getActiveUnconsciousModeOptions();
            if (!options.downedStateAllowed() || bloodSystem.hasBledOut())
                return; // do not allow duplicate rescues

            RandomSource random = entity.getRandom();
            EntityBloodSystemDefinition definition = bloodSystem.getDefinition();
            // Start unconscious mode if allowed instead of death
            if (definition.isDownedStateEnabled() && random.nextFloat() < config.bloodSystem.unconsciousOnDeathChance) {
                HealthContainer container = HealthContainer.getAttached(entity);
                // Prevent unconscious mode if head limb died and config disallows this case
                if (config.bloodSystem.unconsciousOnHeadDeathChance > 0.0F && random.nextFloat() >= config.bloodSystem.unconsciousOnHeadDeathChance) {
                    // no head body part alive, terminate further processing logic
                    if (HealthHelper.allLimbsDead(container, LimbType.HEAD))
                        return;
                }
                event.setCanceled(true);

                // recover vital body part health - otherwise entity would immediately "die" again
                HealthHelper.recoverVitalLimbs(container, 1.0F);

                // make other mobs peaceful towards this entity
                this.clearAttackTargetsAround(entity, 48.0D);

                HealthHelper.synchronizeHealth(entity, container);
                HealthSystem.synchronizeEntity(entity);

                // set unconscious
                bloodSystem.setUnconscious(config.bloodSystem.rescueWaitDuration, UnconsciousOptions.DOWNED);
                bloodSystem.synchronizeImmediately(entity);

                // set a short invulnerability window to prevent immediate follow-up damage
                entity.invulnerableTime = 30;
            }
        }

        if (!event.isCanceled() && HealthSystem.hasCustomHealth(entity)) {
            HealthContainer container = HealthContainer.getAttached(entity);
            container.invalidate();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    private void addItemStackTooltips(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item.TooltipContext context = event.getContext();
        List<Component> tooltip = event.getToolTip();
        TooltipFlag flag = event.getFlags();
        Consumer<Component> adder = tooltip::add;

        stack.addToTooltip(MedSystemItemComponents.ARMOR_MATERIAL, context, TooltipDisplay.DEFAULT, adder, flag);
        stack.addToTooltip(MedSystemItemComponents.ARMOR_RATING, context, TooltipDisplay.DEFAULT, adder, flag);
        stack.addToTooltip(MedSystemItemComponents.HEAL_ATTRIBUTES, context, TooltipDisplay.DEFAULT, adder, flag);
        stack.addToTooltip(MedSystemItemComponents.SIDE_EFFECTS, context, TooltipDisplay.DEFAULT, adder, flag);
    }

    @SubscribeEvent
    private void onItemUseFinished(LivingEntityUseItemEvent.Finish event) {
        ItemStack stack = event.getItem();
        LivingEntity entity = event.getEntity();
        if (stack.has(MedSystemItemComponents.SIDE_EFFECTS)) {
            SideEffectHolder holder = stack.get(MedSystemItemComponents.SIDE_EFFECTS);
            InteractionTarget target = stack.get(MedSystemItemComponents.INTERACTION_TARGET);
            String targetLimb = target != null ? target.limbCode() : null;
            LivingEntity targetEntity = entity;
            if (target != null) {
                targetEntity = target.getTargetLivingEntity(entity);
            }
            if (!HealthSystem.hasCustomHealth(targetEntity))
                return;
            HealthContainer container = HealthContainer.getAttached(targetEntity);
            Limb part = container.getLimbByCode(targetLimb);
            holder.onConsume(targetEntity, container, part);
        }

        if (HealthSystem.hasCustomHealth(entity)) {
            HealthContainer container = HealthContainer.getAttached(entity);
            Limb limb = container.getRootLimb();
            HealthEventContext context = HealthEventContext.withParams(entity, container, limb, builder -> builder.add(HealthEventParams.ITEM, stack));
            MedicalSystem.STATUS_EFFECT_EVENTS.triggerEvent(MedSystemHealthEventSources.CONSUME, context);
        }
    }

    @SubscribeEvent
    private void canMountEntity(EntityMountEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity livingEntity && BloodSystemManager.isUnconscious(livingEntity)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    private void adjustHitboxSize(EntityEvent.Size event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity livingEntity))
            return;
        if (BloodSystemManager.isUnconscious(livingEntity) && !livingEntity.isPassenger()) {
            EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(livingEntity);
            EntityDimensions scalableDim = bloodSystem.getDefinition().getDimensionsForUnconsciousMode();
            event.setNewSize(scalableDim);
        }
    }

    @SubscribeEvent
    private void onSetAttackTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (!event.isCanceled() && newTarget != null && BloodSystemManager.isEnabled(newTarget)) {
            EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(newTarget);
            UnconsciousOptions options = bloodSystem.getActiveUnconsciousModeOptions();
            if (bloodSystem.isUnconscious() && options.allowRescue()) {
                event.setNewAboutToBeSetTarget(null);
            }
        }
    }

    @SubscribeEvent
    private void onItemEntityPickUp(ItemEntityPickupEvent.Pre event) {
        if (event.canPickup().isFalse())
            return;
        Player player = event.getPlayer();
        if (BloodSystemManager.isUnconscious(player)) {
            event.setCanPickup(TriState.FALSE);
        }
    }

    private void clearAttackTargetsAround(LivingEntity victim, double range) {
        Level level = victim.level();
        List<Mob> mobs = level.getEntitiesOfClass(Mob.class, victim.getBoundingBox().inflate(range));
        for (Mob mob : mobs) {
            if (mob.getTarget() == victim) {
                mob.setTarget(null);
                Brain<?> brain = mob.getBrain();
                this.eraseMemory(brain, MemoryModuleType.ANGRY_AT);
                this.eraseMemory(brain, MemoryModuleType.ATTACK_TARGET);
            }
        }
    }

    private void eraseMemory(Brain<?> brain, MemoryModuleType<?> type) {
        if (brain.hasMemoryValue(type)) {
            brain.eraseMemory(type);
        }
    }
}
