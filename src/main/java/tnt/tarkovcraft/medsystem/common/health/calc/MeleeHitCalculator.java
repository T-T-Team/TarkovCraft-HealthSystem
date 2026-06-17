package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.util.HitboxHelper;

import java.util.Comparator;
import java.util.List;

public final class MeleeHitCalculator implements HitCalculator {

    public static final MeleeHitCalculator INSTANCE = new MeleeHitCalculator();

    private MeleeHitCalculator() {
    }

    @Override
    public HitCalculationResult calculateHits(HitCalculationContext context) {
        Entity attacker = context.getAttackingEntity();
        LivingEntity entity = context.entity();
        double distance = attacker.distanceTo(entity) + Math.max(entity.getBbWidth(), entity.getBbHeight());

        Vec3 from = attacker.getType() == EntityTypes.PLAYER ? attacker.getEyePosition() : new Vec3(attacker.getX(), attacker.getY() + attacker.getBbHeight() / 2.0, attacker.getZ());
        Vec3 to = from.add(attacker.getHeadLookAngle().scale(distance));
        Ray ray = new Ray(from, to);

        List<HitInfo> hits = HitboxHelper.raycast(ray, context)
                .sorted(Comparator.comparingDouble(hit -> hit.entryPoint().distanceToSqr(from)))
                .toList();

        if (!hits.isEmpty()) {
            HitInfo closest = hits.getFirst();
            return HitCalculationResult.of(closest)
                    .withRayCast(ray);
        }

        return context.approximate(ray);
    }
}
