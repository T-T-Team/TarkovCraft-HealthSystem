package tnt.tarkovcraft.medsystem.common.health.calc;

import java.util.function.Function;
import java.util.function.Predicate;

public record HitCalculatorRule(int priority, Predicate<HitCalculationContext> filter, Function<HitCalculationContext, HitCalculator> factory) {

    public static int SPECIFIC_PART = -1_000;
    public static int ENVIRONMENT = -750;
    public static int EFFECTS = -500;
    public static int GENERIC = 100;
    public static int MELEE = 200;
    public static int PROJECTILE = 300;

    public boolean validate(HitCalculationContext ctx) {
        return filter.test(ctx);
    }

    public HitCalculator createCalculator(HitCalculationContext ctx) {
        return factory.apply(ctx);
    }
}
