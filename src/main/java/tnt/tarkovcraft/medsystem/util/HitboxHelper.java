package tnt.tarkovcraft.medsystem.util;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.EntityHitboxContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainerDefinition;
import tnt.tarkovcraft.medsystem.common.health.calc.*;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public final class HitboxHelper {

    private static final Generator[] TRACE_GENERATORS = initTraceGenerators();
    private static final int PERF_TRACES = 3;

    public static Stream<HitInfo> approximateHits(Ray ray, LivingEntity entity, HealthContainer container) {
        return getEntityHitboxes(entity, container)
                .map(limbHitBox -> HitInfo.create(limbHitBox, entity))
                .sorted(Comparator.comparingDouble(info -> distanceToRaySqr(ray, info.entryPoint())));
    }

    public static Stream<HitInfo> approximateHits(Ray ray, LivingEntity entity) {
        return approximateHits(ray, entity, HealthContainer.getAttached(entity));
    }

    public static double distanceToRaySqr(Ray ray, Vec3 point) {
        Vec3 ab = ray.direction();
        Vec3 ac = ray.startDirectionTo(point);
        double areaSqr = ab.cross(ac).lengthSqr();
        double baseSqr = ab.lengthSqr();
        return areaSqr / baseSqr;
    }

    public static Stream<LimbHitbox> getEntityHitboxes(LivingEntity entity) {
        HealthContainer container = HealthContainer.getAttached(entity);
        return getEntityHitboxes(entity, container);
    }

    public static Stream<LimbHitbox> getEntityHitboxes(HitCalculationContext context) {
        return getEntityHitboxes(context.entity(), context.container());
    }

    public static Stream<LimbHitbox> getEntityHitboxes(LivingEntity entity, HealthContainer container) {
        HealthContainerDefinition definition = container.getDefinition();
        String activeState = definition.getCurrentEntityState(entity);
        EntityHitboxContainer hitboxContainer = definition.hitboxContainer();
        return container.getLimbContainer().getLimbs()
                .map(limb -> {
                    String code = limb.getLimbCode();
                    EntityHitboxContainer.LimbHitboxDefinition hitboxDefinition = hitboxContainer.getLimbHitbox(code, activeState);
                    return new LimbHitbox(hitboxDefinition, limb);
                });
    }

    public static List<LimbHitbox> getEntityHitboxList(LivingEntity entity) {
        return getEntityHitboxes(entity).toList();
    }

    public static List<LimbHitbox> getEntityHitboxList(LivingEntity entity, HealthContainer container) {
        return getEntityHitboxes(entity, container).toList();
    }

    public static List<LimbHitbox> getEntityHitboxList(HitCalculationContext ctx) {
        return getEntityHitboxList(ctx.entity(), ctx.container());
    }

    public static Stream<HitInfo> raycast(Ray ray, LivingEntity entity, HealthContainer container) {
        return getEntityHitboxes(entity, container)
                .map(hitbox -> {
                    AABB worldspaceAABB = hitbox.worldspaceAABB(entity);
                    Vec3 entry = raycast(ray, worldspaceAABB);
                    return entry != null ? HitInfo.create(hitbox, worldspaceAABB, entry) : null;
                })
                .filter(Objects::nonNull);
    }

    public static Stream<HitInfo> raycast(Ray ray, HitCalculationContext ctx) {
        return raycast(ray, ctx.entity(), ctx.container());
    }

    public static Vec3 getTopCenter(AABB aabb) {
        return new Vec3(Mth.lerp(0.5, aabb.minX, aabb.maxX), aabb.maxY, Mth.lerp(0.5, aabb.minZ, aabb.maxZ));
    }

    public static <T> @Nullable T tracePoint(boolean perfMode, AABB aabb, Function<Vec3, T> trace) {
        int limit = perfMode ? PERF_TRACES : TRACE_GENERATORS.length;
        for (int i = 0; i < limit; i++) {
            Generator generator = TRACE_GENERATORS[i];
            Vec3 entryPoint = generator.generate(aabb);
            T result = trace.apply(entryPoint);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static <T> @Nullable T trace(boolean perfMode, AABB aabb, Vec3 entryPoint, Function<Ray, T> trace) {
        int limit = perfMode ? PERF_TRACES : TRACE_GENERATORS.length;
        for (int i = 0; i < limit; i++) {
            Generator generator = TRACE_GENERATORS[i];
            Ray ray = generator.generateRay(aabb, entryPoint);
            T result = trace.apply(ray);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static @Nullable Vec3 raycast(Ray ray, AABB aabb) {
        return PositionedAABB.tryIntersect(aabb, ray)
                .orElse(null);
    }

    private static Generator[] initTraceGenerators() {
        return new Generator[] {
                // center, bottom center, top center
                AABB::getCenter,
                AABB::getBottomCenter,
                HitboxHelper::getTopCenter,
                // bottom corners
                aabb -> new Vec3(aabb.minX, aabb.minY, aabb.minZ),
                aabb -> new Vec3(aabb.minX, aabb.minY, aabb.maxZ),
                aabb -> new Vec3(aabb.maxX, aabb.minY, aabb.maxZ),
                aabb -> new Vec3(aabb.maxX, aabb.minY, aabb.minZ),
                // top corners
                aabb -> new Vec3(aabb.minX, aabb.maxY, aabb.minZ),
                aabb -> new Vec3(aabb.minX, aabb.maxY, aabb.maxZ),
                aabb -> new Vec3(aabb.maxX, aabb.maxY, aabb.maxZ),
                aabb -> new Vec3(aabb.maxX, aabb.maxY, aabb.minZ)
        };
    }

    @FunctionalInterface
    private interface Generator {

        Vec3 generate(AABB aabb);

        default Ray generateRay(AABB aabb, Vec3 entryPoint) {
            return new Ray(entryPoint, this.generate(aabb));
        }
    }
}
