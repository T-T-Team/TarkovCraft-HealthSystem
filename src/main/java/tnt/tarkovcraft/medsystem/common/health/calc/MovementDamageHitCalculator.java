package tnt.tarkovcraft.medsystem.common.health.calc;

import tnt.tarkovcraft.medsystem.common.health.HealthSystem;

public final class MovementDamageHitCalculator implements HitCalculator {

    public static final MovementDamageHitCalculator INSTANCE = new MovementDamageHitCalculator();

    private MovementDamageHitCalculator() {
    }

    @Override
    public HitCalculationResult calculateHits(HitCalculationContext context) {
        return HitCalculationResult.simpleResult(context, hitbox -> HealthSystem.isMovementRestrictedOnLimb(hitbox.limb()));
    }
}
