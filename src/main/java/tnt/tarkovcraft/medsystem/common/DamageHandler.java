package tnt.tarkovcraft.medsystem.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import tnt.tarkovcraft.core.api.MovementStaminaComponent;
import tnt.tarkovcraft.core.common.energy.EnergySystem;
import tnt.tarkovcraft.core.common.skill.SkillSystem;
import tnt.tarkovcraft.core.common.statistic.StatisticTracker;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.client.config.BloodDecalConfig;
import tnt.tarkovcraft.medsystem.client.particle.BloodDripParticleOptions;
import tnt.tarkovcraft.medsystem.common.armor.ArmorComponent;
import tnt.tarkovcraft.medsystem.common.armor.ArmorSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodConfiguration;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodTypeOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.damage.DamageResolver;
import tnt.tarkovcraft.medsystem.common.health.*;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationResult;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationResultDebugInfo;
import tnt.tarkovcraft.medsystem.common.health.calc.HitInfo;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventParams;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventSources;
import tnt.tarkovcraft.medsystem.common.init.MedSystemSkillEvents;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStats;
import tnt.tarkovcraft.medsystem.util.HealthHelper;

import javax.annotation.Nullable;
import java.util.Map;

public final class DamageHandler {

    private static HitCalculationResultDebugInfo hitDebugInfo = null;

    // Hitbox collision detection
    @SubscribeEvent(priority = EventPriority.LOWEST)
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
        if (BloodSystemManager.isUnconscious(livingEntity) && source.is(DamageTypes.IN_WALL)) {
            event.setInvulnerable(true);
            return;
        }

        HitCalculationContext context = new HitCalculationContext(livingEntity, container, source);
        DamageResolver resolver = MedicalSystem.DAMAGE_RESOLVER.getResolver(context);
        HitCalculationResult result = resolver.calculate(context);

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

        livingEntity.getExistingData(MedSystemDataAttachments.DAMAGE_CONTEXT)
                .ifPresent(ctx -> ctx.init(context, result));
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
        if (event.isCanceled()) {
            entity.getExistingData(MedSystemDataAttachments.DAMAGE_CONTEXT)
                    .ifPresent(DamageContext::reset);
            return;
        }
        ArmorSystem armorSystem = MedicalSystem.getConfig().armor.armorSystem;
        ArmorComponent component = armorSystem.getComponent();
        entity.getExistingData(MedSystemDataAttachments.DAMAGE_CONTEXT).ifPresent(context -> {
            if (!context.isInitialized())
                return;
            component.applyDamageReduction(event, context);
        });
    }

    // Entity damage application
    @SubscribeEvent
    private void onLivingApplyDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        DamageSource damageSource = event.getSource();
        HealthContainer container = HealthContainer.getAttached(entity);
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(entity);
        Integer bloodDecalColor = getBloodColor(entity, container, bloodSystem);
        boolean bloodParticlesHandled = false;
        if (container != null) {
            // handle limb health, limb based blood particles
            DamageSource source = event.getSource();
            DamageContext context = entity.getExistingData(MedSystemDataAttachments.DAMAGE_CONTEXT)
                    .orElseThrow(() -> new IllegalStateException("Damage context not set for entity " + entity));
            float damage = event.getNewDamage();
            Map<Limb, Float> distributedDamage = context.getDamage(damage);

            // apply armor skill based on armor reduction
            float armorReduction = event.getReduction(DamageContainer.Reduction.ARMOR);
            if (armorReduction > 0.0F) {
                SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.ARMOR_USE, entity, armorReduction);
            }

            // apply health container damage
            LimbContainer limbContainer = container.getLimbContainer();
            limbContainer.hurt(context, damage);
            float totalDamage = distributedDamage.values().stream().reduce(0.0F, Float::sum);
            int lostLimbCount = context.getLostLimbsCount();
            if (totalDamage > 0.0F) {
                context.triggerAdvancements(entity);
                // ignore skill leveling from /kill commands and other invulnerability bypassing effects - could be problematic for
                // specific projectile damage sources... maybe instead the max per-event progress amount should be limited
                if (!damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                    SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.DAMAGE_TAKEN, entity, totalDamage);
                    // apply post-damage effects
                    triggerStatusEffectEvent(entity, container, context, distributedDamage, totalDamage, lostLimbCount);
                    // blood decals
                    if (!context.getHits().isEmpty() && bloodDecalColor != null) {
                        HitInfo hit = context.getHits().getFirst();
                        Limb damagedLimb = hit.limb();
                        HealthContainerDefinition definition = container.getDefinition();
                        EntityHitboxContainer hitboxContainer = definition.hitboxContainer();
                        String entityState = definition.getCurrentEntityState(entity);
                        Vec3 pos;
                        if (hit.entryPoint() != null) {
                            pos = hit.entryPoint();
                        } else {
                            AABB aabb = hitboxContainer.getLimbHitbox(damagedLimb.getLimbCode(), entityState).toWorldSpaceHitbox(entity);
                            pos = aabb.getCenter();
                        }
                        addBloodParticles(entity, damageSource, bloodDecalColor, pos, totalDamage);
                    }
                }
            }
            // mark blood particle processing as done
            bloodParticlesHandled = true;

            // Clean data and apply
            entity.getExistingData(MedSystemDataAttachments.DAMAGE_CONTEXT)
                    .ifPresent(DamageContext::reset);
            HealthHelper.synchronizeHealth(entity, container);

            // Death processing
            HealthSystem.synchronizeEntity(entity); // send status to a client before death or further processing so that a client knows which body part caused death
            if (container.isDead()) {
                entity.setHealth(0.0F); // cannot use LivingEntity#die as that causes problems with xp/drops
                return;
            }

            // limbs lost statistic - after death processing to avoid counting lost limbs on entity death
            if (lostLimbCount > 0) {
                StatisticTracker.incrementOptional(entity, MedSystemStats.LIMBS_LOST, lostLimbCount);
            }

            // disable sprinting if an entity can no longer sprint
            MovementStaminaComponent component = EnergySystem.MOVEMENT_STAMINA.getComponent();
            if (entity.isSprinting() && !component.canSprint(entity)) {
                entity.setSprinting(false);
            }
        }

        // handle default blood particles
        if (!bloodParticlesHandled && bloodDecalColor != null && event.getNewDamage() > 0.0F) {
            AABB box = entity.getBoundingBox();
            addBloodParticles(entity, damageSource, bloodDecalColor, box.getCenter(), event.getNewDamage());
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
        ArmorSystem system = config.armor.armorSystem;
        ArmorComponent component = system.getComponent();
        entity.getExistingData(MedSystemDataAttachments.DAMAGE_CONTEXT)
                .ifPresent(context -> component.applyItemDamage(event, context));
    }

    private void triggerStatusEffectEvent(LivingEntity entity, HealthContainer container, DamageContext context, Map<Limb, Float> damage, float total, int lostLimbs) {
        // global damage event
        HealthEventContext globalCtx = HealthEventContext.withParams(entity, container, container.getRootLimb(), builder -> {
            builder.add(HealthEventParams.DAMAGE_CONTEXT, context);
            builder.add(HealthEventParams.DAMAGE_AMOUNT, total);
            builder.add(HealthEventParams.LIMBS_LOST, lostLimbs);
        });
        MedicalSystem.HEALTH_EVENT.triggerEvent(MedSystemHealthEventSources.INCOMING_DAMAGE_GLOBAL, globalCtx);

        // per limb damage triggers
        for (Map.Entry<Limb, Float> entry : damage.entrySet()) {
            Limb limb = entry.getKey();
            float localDamage = entry.getValue();
            HealthEventContext ctx = HealthEventContext.withParams(entity, container, limb, builder -> {
                builder.add(HealthEventParams.DAMAGE_CONTEXT, context);
                builder.add(HealthEventParams.DAMAGE_AMOUNT, total);
                builder.add(HealthEventParams.DAMAGE_AMOUNT_LIMB, localDamage);
            });
            MedicalSystem.HEALTH_EVENT.triggerEvent(MedSystemHealthEventSources.INCOMING_DAMAGE, ctx);
        }
    }

    public static @Nullable Integer getBloodColor(LivingEntity entity, @Nullable HealthContainer container, @Nullable EntityBloodSystem bloodSystem) {
        MedSystemConfig config = MedicalSystem.getConfig();
        if (!config.bloodDecals.enableBloodDecals) {
            return null;
        }
        if (bloodSystem != null) {
            ResourceLocation bloodType = bloodSystem.getBloodType();
            BloodConfiguration bloodConfiguration = MedicalSystem.BLOOD_SYSTEM.getConfig();
            BloodTypeOptions options = bloodConfiguration.getOptions(bloodType).orElse(null);
            if (options != null) {
                return options.color();
            }
        }
        if (container != null) {
            HealthContainerDefinition definition = container.getDefinition();
            return definition.decalSettings().getColor(entity);
        }
        return config.bloodDecals.enableGenericBloodDecals
                ? Integer.decode(config.bloodDecals.bloodDecalColor)
                : null;
    }

    private void addBloodParticles(LivingEntity entity, DamageSource source, int color, Vec3 position, float damage) {
        BloodDecalConfig config = MedicalSystem.getConfig().bloodDecals;
        int particleCount = Math.min(Mth.floor(damage / config.damageDecalScale), config.maxDamageDecalsPerHit);
        if (particleCount <= 0)
            return;
        Vec3 origin = source.getSourcePosition();
        Vec3 direction = origin != null ? entity.position().subtract(origin) : entity.position();
        double length = direction.horizontalDistance();
        boolean projectile = source.is(DamageTypeTags.IS_PROJECTILE);
        float motionScale = projectile ? config.projectileDamageMotionScale : config.damageMotionScale;
        direction = new Vec3(direction.x / length * motionScale, 0.0, direction.z / length * motionScale);
        float deviateAmount = 0.05F;
        BloodDripParticleOptions options = new BloodDripParticleOptions(color);
        double baseDelta = (deviateAmount * 2.0F) - deviateAmount;
        double dx = direction.x + baseDelta;
        double dy = 0.05F;
        double dz = direction.z + baseDelta;
        HealthHelper.submitServerBleedParticles(options, particleCount, position.x, position.y, position.z, dx, dy, dz, entity);
    }

    public static HitCalculationResultDebugInfo getHitDebugInfo() {
        return hitDebugInfo;
    }
}
