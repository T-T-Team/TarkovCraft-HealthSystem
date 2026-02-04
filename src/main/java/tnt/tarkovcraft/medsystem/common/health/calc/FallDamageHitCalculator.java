package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.tags.DamageTypeTags;
import tnt.tarkovcraft.medsystem.common.health.distributor.ScaledDamageDistributor;

public final class FallDamageHitCalculator implements HitCalculator {

    public static final FallDamageHitCalculator INSTANCE = new FallDamageHitCalculator();

    private FallDamageHitCalculator() {
    }

    public static boolean isFall(HitCalculationContext context) {
        return context.isDamageType(DamageTypeTags.IS_FALL);
    }

    @Override
    public HitCalculationResult calculateHits(HitCalculationContext context) {
        return HitCalculationResult.simpleResult(context, hitbox -> hitbox.limb().isLeg())
                .withDamageDistributor(original -> new ScaledDamageDistributor(1.7F, original));
    }
}
