package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.distributor.DamageDistributor;
import tnt.tarkovcraft.medsystem.util.HitboxHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ExplosionHitCalculator(float damageScale, float airPressureMultiplier, float waterPressureMultiplier) implements HitCalculator {

    public static final ResourceLocation METADATA_PRESSURE_FLAG = MedicalSystem.createIdentifier("pressure");

    private static Entity getSourceEntity(HitCalculationContext context) {
        return context.getProjectile();
    }

    private static @Nullable Vec3 resolveSourcePosition(HitCalculationContext context) {
        if (context.hasDamagePosition()) {
            return context.source().getSourcePosition().add(0, 0.5, 0);
        }
        Entity sourceEntity = getSourceEntity(context);
        if (sourceEntity != null) {
            return sourceEntity.getBoundingBox().getCenter();
        }
        return null;
    }


    @Override
    public HitCalculationResult calculateHits(HitCalculationContext context) {
        Vec3 explosionPosition = resolveSourcePosition(context);
        Entity sourceEntity = getSourceEntity(context);
        List<Ray> usedTraces = new ArrayList<>();
        HealthContainer container = context.container();
        int limbCount = container.getDefinition().limbConfiguration().getLimbCount();
        LivingEntity entity = context.entity();
        List<LimbHitbox> hitboxes = HitboxHelper.getEntityHitboxList(entity, container);

        List<HitInfo> hits = new ArrayList<>(hitboxes.size());
        for (LimbHitbox hitbox : hitboxes) {
            AABB aabb = hitbox.worldspaceAABB(entity);
            HitInfo info;
            Vec3 hitLoc = this.clipLimb(aabb, explosionPosition, entity, usedTraces);
            if (hitLoc != null) {
                // apply explosion damage + pressure damage
                info = HitInfo.create(hitbox, aabb, hitLoc);
            } else {
                // applies only pressure damage
                HitInfo.Mutable mutable = HitInfo.createMutable(hitbox);
                mutable.setAABB(aabb);
                mutable.setMetadataParam(METADATA_PRESSURE_FLAG, Unit.INSTANCE);
                info = mutable.toImmutable();
            }
            hits.add(info);
        }

        // More limbs hit = bigger explosion damage share applied
        int damagedLimbs = Math.min(limbCount, hits.size());
        float envPressureMultiplier = sourceEntity != null && sourceEntity.isInWater() && context.entity().isInWater() ? this.waterPressureMultiplier : this.airPressureMultiplier;
        float limbDamageScale = (this.damageScale / limbCount) * damagedLimbs;
        HitCalculationResult result = HitCalculationResult.of(hits);
        usedTraces.forEach(result::withRayCast);
        result.withDamageDistributor(original -> new ExplosionDamageDistributor(limbDamageScale, envPressureMultiplier));
        return result;
    }

    private @Nullable Vec3 clipLimb(AABB aabb, Vec3 position, LivingEntity entity, List<Ray> traceOutput) {
        MedSystemConfig config = MedicalSystem.getConfig();
        return HitboxHelper.trace(config.useExplosionPerformanceMode, aabb, position, ray -> this.clipLimb(ray, entity, traceOutput));
    }

    private @Nullable Vec3 clipLimb(Ray ray, LivingEntity entity, List<Ray> traceOutput) {
        ClipContext context = new ClipContext(ray.from(), ray.to(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
        Level level = entity.level();
        BlockHitResult result = level.clip(context);
        if (result.getType() == HitResult.Type.MISS) {
            traceOutput.add(ray);
            return result.getLocation();
        }
        Vec3 hitPos = result.getLocation();
        traceOutput.add(new Ray(ray.from(), hitPos));
        return null;
    }

    private record ExplosionDamageDistributor(float damageScale, float pressureScale) implements DamageDistributor {

        @Override
        public Map<Limb, Float> distribute(DamageContext context, float damage) {
            Map<Limb, Float> damageMap = new HashMap<>();
            List<HitInfo> hits = context.getHits();
            float totalDamage = this.damageScale * damage;
            float explosionDamagePerLimb = totalDamage / hits.size();
            float scaledPressureDamage = this.pressureScale * totalDamage;
            MedicalSystem.LOGGER.debug("Splitting explosion damage, {} total, {} pressure damage", totalDamage, scaledPressureDamage);
            for (HitInfo hitInfo : hits) {
                boolean isFullHit = !hitInfo.hasMetadataValue(METADATA_PRESSURE_FLAG);
                float limbDamage = scaledPressureDamage;
                if (isFullHit) {
                    limbDamage += explosionDamagePerLimb;
                }
                damageMap.put(hitInfo.limb(), limbDamage);
            }
            return damageMap;
        }
    }
}
