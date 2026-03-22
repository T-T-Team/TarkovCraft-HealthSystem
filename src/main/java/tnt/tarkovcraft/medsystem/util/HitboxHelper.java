package tnt.tarkovcraft.medsystem.util;

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
import java.util.stream.Stream;

public final class HitboxHelper {

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
        return container.getLimbsAsStream()
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

    private static @Nullable Vec3 raycast(Ray ray, AABB aabb) {
        return PositionedAABB.tryIntersect(aabb, ray)
                .orElse(null);
    }
}
