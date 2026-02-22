package tnt.tarkovcraft.medsystem.common;

import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import tnt.tarkovcraft.core.api.MovementStaminaComponent;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.energy.EnergySystem;
import tnt.tarkovcraft.core.common.skill.SkillSystem;
import tnt.tarkovcraft.core.common.statistic.StatisticTracker;
import tnt.tarkovcraft.core.network.message.S2C_MakeParticles;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.client.config.BloodDecalConfig;
import tnt.tarkovcraft.medsystem.common.armor.ArmorComponent;
import tnt.tarkovcraft.medsystem.common.armor.ArmorSystem;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.config.TimeRange;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventParams;
import tnt.tarkovcraft.medsystem.common.health.*;
import tnt.tarkovcraft.medsystem.common.health.calc.*;
import tnt.tarkovcraft.medsystem.common.init.*;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DamageHandler {

    private static HitCalculationResultDebugInfo hitDebugInfo = null;

    // Hitbox collision detection
    @SubscribeEvent
    private void onInvulnerabilityCheck(EntityInvulnerabilityCheckEvent event) {
        if (event.isInvulnerable())
            return;

        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity livingEntity))
            return;
        if (!HealthSystem.hasCustomHealth(livingEntity))
            return;

        HealthContainer container = entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        DamageSource source = event.getSource();

        // no in-block damage when unconscious
        if (BloodSystem.isEntityUnconscious(livingEntity) && source.is(DamageTypes.IN_WALL)) {
            event.setInvulnerable(true);
            return;
        }

        HitCalculationContext context = new HitCalculationContext(livingEntity, container, source);
        HitCalculator hitCalculator = MedicalSystem.HEALTH_SYSTEM.getHitCalculator(context);
        HitCalculationResult result = hitCalculator.calculateHits(context);

        MedSystemConfig config = MedicalSystem.getConfig();
        if (config.enableHitDebug) {
            Level level = entity.level();
            MinecraftServer server = level.getServer();
            if (!level.isClientSide() && !server.isDedicatedServer()) {
                hitDebugInfo = HitCalculationResultDebugInfo.collectDebugData(context, result);
            }
        }

        if (result.isMiss()) {
            event.setInvulnerable(true);
            return;
        }

        DamageContext damageContext = new DamageContext(context, result);
        livingEntity.setData(MedSystemDataAttachments.ACTIVE_DAMAGE_CONTEXT, damageContext);
    }

    // Entity armor damage recalculation
    @SubscribeEvent
    private void onLivingDamage(LivingIncomingDamageEvent event) {
        // calculate correct damage for armor and so on
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        if (!HealthSystem.hasCustomHealth(entity) || source.is(DamageTypeTags.BYPASSES_ARMOR))
            return;
        if (entity.level().isClientSide())
            return;
        ArmorSystem armorSystem = MedicalSystem.getConfig().armorSystem;
        ArmorComponent component = armorSystem.getComponent();
        entity.getExistingData(MedSystemDataAttachments.ACTIVE_DAMAGE_CONTEXT).ifPresent(context -> {
            component.applyDamageReduction(event, context);
            float armorReduction = event.getContainer().getReduction(DamageContainer.Reduction.ARMOR);
            if (armorReduction > 0.0F) {
                SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.ARMOR_USE, entity, armorReduction);
            }
        });
    }

    // Entity damage application
    @SubscribeEvent
    private void onLivingApplyDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (!HealthSystem.hasCustomHealth(entity))
            return;
        HealthContainer container = entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        DamageSource source = event.getSource();
        DamageContext context = entity.getExistingData(MedSystemDataAttachments.ACTIVE_DAMAGE_CONTEXT)
                .orElseThrow(() -> new IllegalStateException("Damage context not set for entity " + entity));
        float damage = event.getNewDamage();
        Map<Limb, Float> distributedDamage = context.getDamage(damage);
        List<Limb> lostLimbs = new ArrayList<>();

        // apply health container damage
        container.hurt(context, distributedDamage, lostLimbs::add);

        // ignore skill leveling from /kill commands and other invulnerability bypassing effects - could be problematic for
        // specific projectile damage sources... maybe instead the max per-event progress amount should be limited
        float totalDamage = distributedDamage.values().stream().reduce(0.0F, Float::sum);
        if (totalDamage > 0.0F) {
            if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.DAMAGE_TAKEN, entity, totalDamage);
            }
            // apply post-damage effects
            this.triggerStatusEffectEvent(entity, container, context, distributedDamage, totalDamage);
            // blood decals
            this.addBloodParticles(entity, source, container, context, totalDamage);
        }


        // Clean data and apply
        entity.removeData(MedSystemDataAttachments.ACTIVE_DAMAGE_CONTEXT);
        container.updateHealth(entity);

        // Death processing
        HealthSystem.synchronizeEntity(entity); // send status to a client before death or further processing so that a client knows which body part caused death
        if (container.shouldDie()) {
            entity.setHealth(0.0F); // cannot use LivingEntity#die as that causes problems with xp/drops
            return;
        }

        // limbs lost statistic - after death processing to avoid counting lost limbs on entity death
        int lostLimbCount = lostLimbs.size();
        if (lostLimbCount > 0) {
            StatisticTracker.incrementOptional(entity, MedSystemStats.LIMBS_LOST, lostLimbCount);
        }

        // Unconscious state processing TODO move to status effect events?
        if (BloodSystem.hasBloodDataIntegration(entity) && !entity.level().isClientSide()) {
            RandomSource random = entity.getRandom();
            BloodData bloodData = BloodSystem.getBloodData(entity);
            MedSystemConfig config = MedicalSystem.getConfig();
            int limbLostCount = lostLimbs.size();
            if (!bloodData.isUnconscious() && config.bloodSystem.unconsciousAfterLimbLossMultiplier > 0.0F && limbLostCount > 0) {
                float unconsciousChance = limbLostCount * AttributeSystem.getFloatValue(entity, MedSystemAttributes.UNCONSCIOUS_ON_LIMB_LOSS_CHANCE, 0.2F) * config.bloodSystem.unconsciousAfterLimbLossMultiplier;
                if (unconsciousChance > 0.0F && random.nextFloat() < unconsciousChance) {
                    TimeRange timeRange = config.bloodSystem.unconsciousOnLimbLoss;
                    int unconsciousTime = 0;
                    for (int i = 0; i < limbLostCount; i++) {
                        unconsciousTime += timeRange.getDurationInSeconds(random);
                    }
                    if (unconsciousTime > 0) {
                        bloodData.setOrExtendedUnconsciousTime(unconsciousTime, BloodData.UnconsciousInfo.PAIN);
                    }
                }
            }

            bloodData.sync(entity);
        }

        // disable sprinting if an entity can no longer sprint
        MovementStaminaComponent component = EnergySystem.MOVEMENT_STAMINA.getComponent();
        if (entity.isSprinting() && !component.canSprint(entity)) {
            entity.setSprinting(false);
        }
    }

    // Armor damaging
    @SubscribeEvent
    private void onArmorHit(ArmorHurtEvent event) {
        if (event.isCanceled())
            return;
        LivingEntity entity = event.getEntity();
        if (!HealthSystem.hasCustomHealth(entity))
            return;
        MedSystemConfig config = MedicalSystem.getConfig();
        ArmorSystem system = config.armorSystem;
        ArmorComponent component = system.getComponent();
        entity.getExistingData(MedSystemDataAttachments.ACTIVE_DAMAGE_CONTEXT)
                .ifPresent(context -> component.applyItemDamage(event, context));
    }

    private void triggerStatusEffectEvent(LivingEntity entity, HealthContainer container, DamageContext context, Map<Limb, Float> damage, float total) {
        for (Map.Entry<Limb, Float> entry : damage.entrySet()) {
            Limb limb = entry.getKey();
            float localDamage = entry.getValue();
            StatusEffectEventContext ctx = StatusEffectEventContext.withParams(entity, container, limb, builder -> {
                builder.add(StatusEffectEventParams.DAMAGE_CONTEXT, context);
                builder.add(StatusEffectEventParams.DAMAGE_AMOUNT, total);
                builder.add(StatusEffectEventParams.DAMAGE_AMOUNT_LIMB, localDamage);
            });
            MedicalSystem.STATUS_EFFECT_EVENTS.triggerEvent(MedSystemStatusEffectEventSources.INCOMING_DAMAGE, ctx);
        }
    }

    private void addBloodParticles(LivingEntity entity, DamageSource source, HealthContainer container, DamageContext context, float damage) {
        BloodDecalConfig config = MedicalSystem.getConfig().bloodDecals;
        if (!config.enableBloodDecals || context.getHits().isEmpty())
            return;
        int particleCount = Math.min(Mth.floor(damage / config.damageDecalScale), config.maxDamageDecalsPerHit);
        if (particleCount <= 0)
            return;
        Vec3 origin = source.getSourcePosition();
        Vec3 direction = origin != null ? entity.position().subtract(origin) : entity.position();
        double length = direction.horizontalDistance();
        boolean projectile = source.is(DamageTypeTags.IS_PROJECTILE);
        float motionScale = projectile ? config.projectileDamageMotionScale : config.damageMotionScale;
        RandomSource random = entity.getRandom();
        direction = new Vec3(direction.x / length * motionScale, 0.0, direction.z / length * motionScale);
        HitInfo result = context.getHits().getFirst();
        Limb mainDamagedLimb = result.limb();
        HealthContainerDefinition definition = container.getDefinition();
        EntityHitboxContainer hitboxContainer = definition.hitboxContainer();
        String entityState = definition.getCurrentEntityState(entity);
        Vec3 pos;
        if (result.entryPoint() != null) {
            pos = result.entryPoint();
        } else {
            AABB aabb = hitboxContainer.getLimbHitbox(mainDamagedLimb.getLimbCode(), entityState).toWorldSpaceHitbox(entity);
            pos = aabb.getCenter();
        }
        List<Vec3> directions = new ArrayList<>();
        float deviateAmount = 0.05F;
        for (int i = 0; i < particleCount; i++) {
            double deviateX = random.nextFloat() * (deviateAmount * 2.0F) - deviateAmount;
            double deviateZ = random.nextFloat() * (deviateAmount * 2.0F) - deviateAmount;
            directions.add(new Vec3(direction.x + deviateX, 0.05F, direction.z + deviateZ));
        }
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new S2C_MakeParticles(MedSystemParticleTypes.BLOOD_DRIP.get(), pos.x, pos.y, pos.z, true, true, directions));
    }

    public static HitCalculationResultDebugInfo getHitDebugInfo() {
        return hitDebugInfo;
    }
}
