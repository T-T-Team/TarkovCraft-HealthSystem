package tnt.tarkovcraft.medsystem.common.health.calc;

import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTags;

public final class MovementDamageHitCalculator implements HitCalculator {

    public static final MovementDamageHitCalculator INSTANCE = new MovementDamageHitCalculator();

    private MovementDamageHitCalculator() {
    }

    public static boolean canApply(HitCalculationContext context) {
        return context.isDamageType(MedSystemTags.DamageTypes.IS_MOVEMENT_RESTRICTED);
    }

    @Override
    public HitCalculationResult calculateHits(HitCalculationContext context) {
        return HitCalculationResult.simpleResult(context, hitbox -> HealthSystem.isMovementRestrictedOnLimb(hitbox.limb()));
    }
}
