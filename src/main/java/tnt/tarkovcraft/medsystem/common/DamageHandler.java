package tnt.tarkovcraft.medsystem.common;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import tnt.tarkovcraft.core.api.MovementStaminaComponent;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.common.energy.EnergySystem;
import tnt.tarkovcraft.core.common.skill.SkillSystem;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.armor.ArmorComponent;
import tnt.tarkovcraft.medsystem.common.armor.ArmorSystem;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContextType;
import tnt.tarkovcraft.medsystem.common.health.*;
import tnt.tarkovcraft.medsystem.common.health.math.DamageDistributor;
import tnt.tarkovcraft.medsystem.common.health.math.HitCalculator;
import tnt.tarkovcraft.medsystem.common.init.MedSystemAttributes;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemSkillEvents;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

import java.util.*;

public final class DamageHandler {

    // FIXME make sure trackers are removed also for disconnected players, forcefully removed entities to prevent memory leaks - add as a data attachment?
    private static final Map<UUID, DamageContext> ACTIVE_DAMAGE_TRACKERS = new HashMap<>();

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
                ACTIVE_DAMAGE_TRACKERS.put(livingEntity.getUUID(), context);
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
        getDamageContext(entity).ifPresent(context -> {
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
        DamageContext context = ACTIVE_DAMAGE_TRACKERS.get(entity.getUUID());
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
        }

        // Clean data and apply
        clearDamageContext(entity);
        container.updateHealth(entity);

        // Death processing
        HealthSystem.synchronizeEntity(entity); // send status to a client before death or further processing so that a client knows which body part caused death
        if (container.shouldDie()) {
            entity.setHealth(0.0F); // cannot use LivingEntity#die as that causes problems with xp/drops
            return;
        }

        // Unconscious state processing
        if (BloodSystem.hasBloodDataIntegration(entity) && !entity.level().isClientSide()) {
            RandomSource random = entity.getRandom();
            BloodData bloodData = BloodSystem.getBloodData(entity);
            MedSystemConfig config = MedicalSystem.getConfig();
            int limbLostCount = lostLimbs.size();
            if (!bloodData.isUnconscious() && config.allowUnconsciousOnLimbLost && limbLostCount > 0) {
                float unconsciousChance = limbLostCount * AttributeSystem.getFloatValue(entity, MedSystemAttributes.UNCONSCIOUS_ON_LIMB_LOSS_CHANCE, 0.2F);
                if (unconsciousChance > 0.0F && random.nextFloat() < unconsciousChance) {
                    int unconsciousDuration = limbLostCount * Duration.seconds(10).tickValue();
                    bloodData.setOrExtendedUnconsciousTime(unconsciousDuration, BloodData.UnconsciousInfo.PAIN);
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
        getDamageContext(entity)
                .ifPresent(context -> component.applyItemDamage(event, context));
    }

    private static Optional<DamageContext> getDamageContext(LivingEntity entity) {
        return Optional.ofNullable(ACTIVE_DAMAGE_TRACKERS.get(entity.getUUID()));
    }

    private static void clearDamageContext(LivingEntity entity) {
        if (entity != null)
            ACTIVE_DAMAGE_TRACKERS.remove(entity.getUUID());
    }
}
