package tnt.tarkovcraft.medsystem.common.health.math;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HitResult;
import tnt.tarkovcraft.medsystem.common.health.PositionedAABB;

import java.util.*;

public class MeleeHitCalculator implements HitCalculator {

    public static final MeleeHitCalculator INSTANCE = new MeleeHitCalculator();

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        List<HitResult> hits = new ArrayList<>();
        Entity attacker = source.getEntity();
        Vec3 from = attacker.getType() == EntityType.PLAYER ? attacker.getEyePosition() : new Vec3(attacker.getX(), attacker.getY() + attacker.getBbHeight() / 2.0, attacker.getZ());
        Vec3 to = from.add(attacker.getLookAngle().scale(5.0));
        // Try to find directly hit body part
        container.acceptHitboxes(
                (hitbox, part) -> {
                    AABB aabb = hitbox.getLevelPositionedAABB(entity);
                    PositionedAABB.tryIntersect(aabb, from, to).ifPresent(hit -> hits.add(new HitResult(hitbox, part, aabb, hit)));
                }
        );
        if (!hits.isEmpty()) {
            hits.sort(Comparator.comparingDouble(res -> res.aabb().distanceToSqr(from)));
            return Collections.singletonList(hits.getFirst());
        }

        // No hitboxes were hit, get closest most likely hit body part
        Vec3 center = attacker.getBoundingBox().getCenter();
        List<HitResult> hitboxPositions = new ArrayList<>();
        container.acceptHitboxes(
                (hitbox, part) -> !part.isDead(),
                (hitbox, part) -> {
                    AABB aabb = hitbox.getLevelPositionedAABB(entity);
                    Vec3 aabbCenter = aabb.getCenter();
                    hitboxPositions.add(new HitResult(hitbox, part, aabb, aabbCenter));
                }
        );
        hitboxPositions.sort(Comparator
                .<HitResult>comparingDouble(res -> res.aabb().getCenter().y - center.y)
                .thenComparingDouble(res -> res.aabb().getCenter().distanceToSqr(center))
        );

        return hitboxPositions.isEmpty() ? Collections.emptyList() : Collections.singletonList(hitboxPositions.getFirst());
    }
}
