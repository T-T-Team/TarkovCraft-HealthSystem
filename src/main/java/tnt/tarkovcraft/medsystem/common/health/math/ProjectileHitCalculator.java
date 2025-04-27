package tnt.tarkovcraft.medsystem.common.health.math;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.*;

import java.util.*;

public class ProjectileHitCalculator implements HitCalculator {

    public static final ProjectileHitCalculator INSTANCE = new ProjectileHitCalculator(0.3);

    private final double aabbInflate;

    public ProjectileHitCalculator(double aabbInflate) {
        this.aabbInflate = aabbInflate;
    }

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        Entity projectile = source.getDirectEntity();
        Vec3 position = projectile.getBoundingBox().getCenter();
        Vec3 destPosition = position.add(projectile.getDeltaMovement().scale(2.5D));
        int pierceAmount = HealthSystem.getProjectilePiercing(entity, source, container, projectile);
        List<BodyPartHitbox> hitboxes = container.getDefinition().getHitboxes();
        List<HitResult> hits = new ArrayList<>();
        for (BodyPartHitbox hitbox : hitboxes) {
            AABB axisAlignedBB = PositionedAABB.inflate(hitbox.getLevelPositionedAABB(entity), this.aabbInflate);
            Optional<Vec3> intersect = PositionedAABB.tryIntersect(axisAlignedBB, position, destPosition);
            intersect.ifPresent(hit -> hits.add(new HitResult(hitbox, container.getBodyPart(hitbox.getOwner()), axisAlignedBB, hit)));
        }
        hits.sort(Comparator.comparingDouble(res -> res.aabb().distanceToSqr(position)));
        if (!hits.isEmpty()) {
            return hits.subList(0, Math.min(hits.size(), pierceAmount));
        }

        List<HitResult> closest = HealthSystem.getClosestPossibleHits(position, entity, container, (hitbox, part) -> true);
        return closest.isEmpty() ? Collections.emptyList() : Collections.singletonList(closest.getFirst());
    }
}
