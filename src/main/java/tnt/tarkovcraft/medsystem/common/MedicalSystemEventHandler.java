package tnt.tarkovcraft.medsystem.common;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import tnt.tarkovcraft.core.api.event.EntityWeightUpdateEvent;
import tnt.tarkovcraft.core.api.event.StaminaEvent;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.effect.OverweightStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageTypes;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;
import tnt.tarkovcraft.medsystem.common.item.InteractionTarget;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodStatus;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

import java.util.List;
import java.util.function.Consumer;

public final class MedicalSystemEventHandler {

    @SubscribeEvent
    private void onEntitySpawn(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.isCanceled())
            return;
        if (entity instanceof LivingEntity livingEntity) {
            MedicalSystem.HEALTH_SYSTEM.getHealthContainer(livingEntity).ifPresent(container -> {
                container.bind(livingEntity);
                HealthSystem.synchronizeEntity(livingEntity);
            });
            if (livingEntity.getType() == EntityType.PLAYER && !BloodSystem.hasBloodDataIntegration(livingEntity)) {
                livingEntity.setData(MedSystemDataAttachments.BLOOD_DATA, new BloodData(5.0F));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    private void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        float amount = event.getAmount();
        if (event.isCanceled())
            return;
        if (amount > 0.0F && HealthSystem.hasCustomHealth(entity)) {
            float leftover = entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER).heal(entity, amount, null);
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
        if (!HealthSystem.hasCustomHealth(entity))
            return;
        HealthContainer container = HealthSystem.getHealthData(entity);
        StatusEffectMap effects = container.getGlobalStatusEffects();
        if (factor > 0.0F) {
            effects.replace(new OverweightStatusEffect(factor >= 1.0F));
        } else {
            effects.remove(StatusEffectSubmitter.NOOP, MedSystemStatusEffects.OVERWEIGHT, container, entity, null);
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
            entity.hurt(MedSystemDamageTypes.causeFractureDamage(access), 0.25F);
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
            entity.hurt(MedSystemDamageTypes.causeFractureDamage(access), 0.50F);
        }
    }

    @SubscribeEvent
    private void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled())
            return;
        LivingEntity entity = event.getEntity();
        MedSystemConfig config = MedicalSystem.getConfig();
        DamageSource source = event.getSource();
        if (!event.isCanceled() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && entity instanceof Player player) {
            BloodData bloodData = BloodSystem.getBloodData(player);
            BloodStatus status = BloodStatus.fromBloodLevelPercentage(bloodData.getBloodVolumePercentage());
            BloodData.UnconsciousInfo unconsciousInfo = bloodData.getUnconsciousInfo();
            if (status == BloodStatus.DEATH || (unconsciousInfo != null && unconsciousInfo.causesDeath()))
                return; // no rescue on full blood loss, small workaround for immediate death
            RandomSource random = player.getRandom();
            if (random.nextFloat() < config.unconsciousOnDeathChance) {
                HealthContainer container = HealthSystem.getHealthData(player);
                if (!config.allowUnconsciousOnHeadDeath) {
                    boolean allPartsDead = container.getLimbsAsStream()
                            .filter(part -> part.getType() == LimbType.HEAD)
                            .allMatch(Limb::isDead);
                    // no head body part alive, terminate further processing logic
                    if (allPartsDead)
                        return;
                }
                event.setCanceled(true);
                // recover vital body part health - otherwise player would immediately "die" again
                container.getLimbsAsStream().forEach(part -> {
                    if (part.isDead() && part.isVital()) {
                        part.heal(1.0F);
                    }
                });
                container.updateHealth(player);
                HealthSystem.synchronizeEntity(player);

                // set unconscious
                bloodData.setUnconsciousTime(
                        Duration.seconds(config.rescueWaitDuration).tickValue(),
                        BloodData.UnconsciousInfo.DEATH
                );
                bloodData.sync(player);
            }
        }

        if (!event.isCanceled() && HealthSystem.hasCustomHealth(entity)) {
            HealthContainer container = HealthSystem.getHealthData(entity);
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

        stack.addToTooltip(MedSystemItemComponents.HEAL_ATTRIBUTES, context, adder, flag);
        stack.addToTooltip(MedSystemItemComponents.SIDE_EFFECTS, context, adder, flag);
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
            HealthContainer container = HealthSystem.getHealthData(targetEntity);
            Limb part = container.getLimbByCode(targetLimb);
            holder.onConsume(targetEntity, container, part);
        }
    }

    @SubscribeEvent
    private void adjustHitboxSize(EntityEvent.Size event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.PLAYER && !entity.isPassenger()) {
            Player player = (Player) entity;
            if (BloodSystem.isEntityUnconscious(player)) {
                event.setNewSize(BloodData.PLAYER_UNCONSCIOUS_DIMENSIONS);
            }
        }
    }

    @SubscribeEvent
    private void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        BloodData data = BloodSystem.getBloodData(player);
        data.updateEffects(player);
    }

    @SubscribeEvent
    private void onSetAttackTarget(LivingChangeTargetEvent event) {
        LivingEntity newTarget = event.getNewAboutToBeSetTarget();
        if (!event.isCanceled() && newTarget instanceof Player player) {
            BloodData data = BloodSystem.getBloodData(player);
            BloodData.UnconsciousInfo info = data.getUnconsciousInfo();
            if (data.isUnconscious() && info.causesDeath()) {
                event.setNewAboutToBeSetTarget(null);
            }
        }
    }
}
