package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
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
import java.util.*;

public final class ExplosionHitCalculator implements HitCalculator {

    public static final ExplosionHitCalculator INSTANCE = new ExplosionHitCalculator();
    public static final ResourceLocation METADATA_PRESSURE_FLAG = MedicalSystem.resource("pressure");
    private static final float EXPLOSION_DAMAGE_SCALING = 2.5F;
    private static final float AIR_PRESSURE_MULTIPLIER = 0.5F;
    private static final float WATER_PRESSURE_MULTIPLIER = 1.2F;

    private ExplosionHitCalculator() {
    }

    public static boolean isExplosion(HitCalculationContext context) {
        return context.isDamageType(DamageTypeTags.IS_EXPLOSION) && resolveSourcePosition(context) != null;
    }

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
        float envPressureMultiplier = sourceEntity != null && sourceEntity.isInWater() && context.entity().isInWater() ? WATER_PRESSURE_MULTIPLIER : AIR_PRESSURE_MULTIPLIER;
        float limbDamageScale = (EXPLOSION_DAMAGE_SCALING / limbCount) * damagedLimbs;
        HitCalculationResult result = HitCalculationResult.of(hits);
        usedTraces.forEach(result::withRayCast);
        result.withDamageDistributor(original -> new ExplosionDamageDistributor(limbDamageScale, envPressureMultiplier));
        return result;
    }

    private Vec3 clipLimb(AABB aabb, Vec3 position, LivingEntity entity, List<Ray> traceOutput) {
        int traceCount = TraceGenerator.rayLimit();
        for (int i = 0; i < traceCount; i++) {
            Ray ray = TraceGenerator.generateRay(i, position, aabb);
            Vec3 hitLoc = this.clipLimb(ray, entity, traceOutput);
            if (hitLoc != null) {
                return hitLoc;
            }
        }
        return null;
    }

    private Vec3 clipLimb(Ray ray, LivingEntity entity, List<Ray> traceOutput) {
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

    private static final class TraceGenerator {

        private static final int PERFORMANCE_MODE_RAYS = 3;
        private static final int DEFAULT_MODE_RAYS = 11;
        private static final RayGenerator[] GENERATORS = new RayGenerator[] {
                // center, bottom center, top center
                (src, aabb) -> Ray.create(src, aabb.getCenter()),
                (src, aabb) -> Ray.create(src, aabb.getBottomCenter()),
                (src, aabb) -> Ray.create(src, new Vec3(Mth.lerp(0.5, aabb.minX, aabb.maxX), aabb.maxY, Mth.lerp(0.5, aabb.minZ, aabb.maxZ))),
                // bottom corners
                (src, aabb) -> Ray.create(src, new Vec3(aabb.minX, aabb.minY, aabb.minZ)),
                (src, aabb) -> Ray.create(src, new Vec3(aabb.minX, aabb.minY, aabb.maxZ)),
                (src, aabb) -> Ray.create(src, new Vec3(aabb.maxX, aabb.minY, aabb.maxZ)),
                (src, aabb) -> Ray.create(src, new Vec3(aabb.maxX, aabb.minY, aabb.minZ)),
                // top corners
                (src, aabb) -> Ray.create(src, new Vec3(aabb.minX, aabb.maxY, aabb.minZ)),
                (src, aabb) -> Ray.create(src, new Vec3(aabb.minX, aabb.maxY, aabb.maxZ)),
                (src, aabb) -> Ray.create(src, new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ)),
                (src, aabb) -> Ray.create(src, new Vec3(aabb.maxX, aabb.maxY, aabb.minZ))
        };

        static int rayLimit() {
            MedSystemConfig config = MedicalSystem.getConfig();
            return config.useExplosionPerformanceMode ? PERFORMANCE_MODE_RAYS : DEFAULT_MODE_RAYS;
        }

        static Ray generateRay(int rayIndex, Vec3 from, AABB context) {
            return GENERATORS[rayIndex].generate(from, context);
        }

        @FunctionalInterface
        private interface RayGenerator {
            Ray generate(Vec3 from, AABB aabb);
        }
    }
}
