package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.distributor.DamageDistributor;
import tnt.tarkovcraft.medsystem.common.health.distributor.DecayingDamageDistributor;

import java.util.*;

public record ProjectileHitCalculator(double aabbInflate) implements HitCalculator {

    public static final ProjectileHitCalculator DEFAULT = new ProjectileHitCalculator(0.3);

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        Entity projectile = source.getDirectEntity();
        Vec3 position = projectile.getBoundingBox().getCenter();
        Vec3 destPosition = position.add(projectile.getDeltaMovement().scale(2.5D));
        int pierceAmount = HealthSystem.getProjectilePiercing(entity, source, container, projectile);
        List<HitResult> hits = new ArrayList<>();
        container.iterateHitboxes(entity, (hitbox, limb) -> {
            AABB worldspaceAabb = PositionedAABB.inflateOutward(hitbox.toWorldSpaceHitbox(entity), this.aabbInflate);
            Optional<Vec3> intersect = PositionedAABB.tryIntersect(worldspaceAabb, position, destPosition);
            intersect.ifPresent(hit -> hits.add(new HitResult(hitbox, limb, worldspaceAabb, hit)));
        });
        hits.sort(Comparator.comparingDouble(res -> res.aabb().distanceToSqr(position)));
        if (!hits.isEmpty()) {
            return hits.subList(0, Math.min(hits.size(), pierceAmount));
        }

        List<HitResult> closest = HealthSystem.getClosestPossibleHits(position, entity, container, (hitbox, part) -> true);
        return closest.isEmpty() ? Collections.emptyList() : Collections.singletonList(closest.getFirst());
    }

    @Override
    public DamageDistributor getCustomDamageDistributor(LivingEntity entity, DamageSource source, HealthContainer container, DamageDistributor original) {
        return DecayingDamageDistributor.PROJECTILE;
    }
}
