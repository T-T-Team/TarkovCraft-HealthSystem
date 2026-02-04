package tnt.tarkovcraft.medsystem.common.health.calc;

public final class GenericHitCalculator implements HitCalculator {

    public static final GenericHitCalculator INSTANCE = new GenericHitCalculator();

    private GenericHitCalculator() {
    }

    @Override
    public HitCalculationResult calculateHits(HitCalculationContext context) {
        return HitCalculationResult.simpleResult(context);
    }
}
