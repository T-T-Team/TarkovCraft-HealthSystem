package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.distributor.DecayingDamageDistributor;
import tnt.tarkovcraft.medsystem.util.HitboxHelper;

import java.util.Comparator;
import java.util.List;

public record ProjectileHitCalculator(float pierceDecay) implements HitCalculator {

    @Override
    public HitCalculationResult calculateHits(HitCalculationContext context) {
        Entity projectile = context.getProjectile();
        Vec3 position = projectile.getBoundingBox().getBottomCenter().subtract(projectile.getDeltaMovement());
        Vec3 destPosition = position.add(projectile.getDeltaMovement().scale(2.5D));
        Ray ray = new Ray(position, destPosition);

        int pierceAmount = HealthSystem.getProjectilePiercing(context);
        List<HitInfo> hits = HitboxHelper.raycast(ray, context)
                .sorted(Comparator.comparingDouble(hit -> hit.entryPoint().distanceToSqr(position)))
                .toList();

        if (!hits.isEmpty()) {
            int limit = Math.min(hits.size(), pierceAmount);
            return HitCalculationResult.of(hits.subList(0, limit))
                    .withRayCast(ray)
                    .withDamageDistributor(_ -> new DecayingDamageDistributor(this.pierceDecay));
        }

        return context.approximate(ray).withDamageDistributor(_ -> new DecayingDamageDistributor(this.pierceDecay));
    }
}
