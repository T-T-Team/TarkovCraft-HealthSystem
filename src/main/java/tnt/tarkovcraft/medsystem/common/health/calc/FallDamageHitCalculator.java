package tnt.tarkovcraft.medsystem.common.health.calc;

import tnt.tarkovcraft.medsystem.common.health.distributor.ScaledDamageDistributor;

public record FallDamageHitCalculator(float damageScale) implements HitCalculator {

    @Override
    public HitCalculationResult calculateHits(HitCalculationContext context) {
        return HitCalculationResult.simpleResult(context, hitbox -> hitbox.limb().isLeg())
                .withDamageDistributor(original -> new ScaledDamageDistributor(this.damageScale, original));
    }
}
