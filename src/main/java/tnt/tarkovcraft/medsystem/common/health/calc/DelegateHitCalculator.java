package tnt.tarkovcraft.medsystem.common.health.calc;

import tnt.tarkovcraft.medsystem.common.health.distributor.DamageDistributor;

public record DelegateHitCalculator(HitCalculator delegate, DamageDistributor distributor) implements HitCalculator {

    @Override
    public HitCalculationResult calculateHits(HitCalculationContext context) {
        return this.delegate.calculateHits(context)
                .withDamageDistributor(original -> this.distributor);
    }
}
