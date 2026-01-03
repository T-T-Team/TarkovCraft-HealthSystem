package tnt.tarkovcraft.medsystem.common;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.config.BloodDecalConfig;
import tnt.tarkovcraft.medsystem.common.armor.ArmorComponent;
import tnt.tarkovcraft.medsystem.common.armor.ArmorSystem;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.config.TimeRange;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContextType;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculator;
import tnt.tarkovcraft.medsystem.common.health.calc.HitResult;
import tnt.tarkovcraft.medsystem.common.health.distributor.DamageDistributor;
import tnt.tarkovcraft.medsystem.common.init.*;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DamageHandler {

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

        HitCalculator hitCalculator = MedicalSystem.HEALTH_SYSTEM.getHitCalculator(livingEntity, source, container);
        List<HitResult> hits = hitCalculator.calculateHits(livingEntity, source, container);
        if (hits == null || hits.isEmpty()) {
            event.setInvulnerable(true);
        } else {
            ArmorSystem armorSystem = MedicalSystem.getConfig().armorSystem;
            ArmorComponent component = armorSystem.getComponent();
            if (!source.is(DamageTypeTags.BYPASSES_ARMOR) && component.shouldDeflectIncomingHit(source, livingEntity, hits)) {
                event.setInvulnerable(true);
            } else {
                DamageContext context = new DamageContext(livingEntity, source);
                context.setHits(hits);
                context.setHitCalculator(hitCalculator);
                context.setSideEffects(SideEffectHolder.fromDamage(source));
                livingEntity.setData(MedSystemDataAttachments.ACTIVE_DAMAGE_CONTEXT, context);
            }
        }
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
        DamageDistributor damageDistributor = context.getDamageDistributor(container);
        Map<Limb, Float> distributedDamage = damageDistributor.distribute(context, container, event.getNewDamage());
        List<Limb> lostLimbs = new ArrayList<>();

        // apply health container damage
        container.hurt(context, distributedDamage, lostLimbs::add);
        float totalDamage = distributedDamage.values().stream().reduce(0.0F, Float::sum);
        if (totalDamage > 0.0F) {
            // ignore skill leveling from /kill commands and other invulnerability bypassing effects - could be problematic for
            // specific projectile damage sources... maybe instead the max per-event progress amount should be limited
            if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.DAMAGE_TAKEN, entity, totalDamage);
            }
            // apply post-damage effects
            MedicalSystem.DAMAGE_EFFECTS.apply(DamageEffectContextType.ON_HURT, effect -> effect.applyDamageEvent(entity, container, context, totalDamage, distributedDamage, lostLimbs));
            // blood decals
            this.addBloodParticles(entity, source, Math.min(totalDamage, entity.getHealth()));
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

        // Unconscious state processing
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

    private void addBloodParticles(LivingEntity entity, DamageSource source, float damage) {
        BloodDecalConfig config = MedicalSystemClient.getConfig().bloodDecals;
        if (!config.enableBloodDecals || !config.enableBloodDecalsOnDamage)
            return;
        int particleCount = Math.min(Mth.floor(damage / config.damageDecalScale), 5);
        if (particleCount <= 0)
            return;
        Vec3 origin = source.getSourcePosition();
        Vec3 direction;
        if (origin != null) {
            direction = entity.position().subtract(origin);
        } else {
            direction = entity.position();
        }
        double length = direction.horizontalDistance();
        double scale = config.damageMotionScale;
        RandomSource random = entity.getRandom();
        direction = new Vec3(direction.x / length * scale, 0.0, direction.z / length * scale);
        Vec3 pos = entity.getBoundingBox().getCenter();
        List<Vec3> directions = new ArrayList<>();
        float deviateAmount = 0.05F;
        for (int i = 0; i < particleCount; i++) {
            double deviateX = random.nextFloat() * (deviateAmount * 2.0F) - deviateAmount;
            double deviateZ = random.nextFloat() * (deviateAmount * 2.0F) - deviateAmount;
            directions.add(new Vec3(direction.x + deviateX, 0.05F, direction.z + deviateZ));
        }
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, new S2C_MakeParticles(MedSystemParticleTypes.BLOOD_DRIP.get(), pos.x, pos.y, pos.z, true, true, directions));
    }
}
