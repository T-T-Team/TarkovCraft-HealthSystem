package tnt.tarkovcraft.medsystem.common.damage.function;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculator;
import tnt.tarkovcraft.medsystem.common.health.calc.MeleeHitCalculator;

public final class MeleeHitFunction implements DamageFunction {

    public static final MapCodec<MeleeHitFunction> CODEC = MapCodec.unit(MeleeHitFunction::new);

    @Override
    public HitCalculator resolve(HitCalculationContext context) {
        return MeleeHitCalculator.INSTANCE;
    }

    @Override
    public MapCodec<? extends DamageFunction> codec() {
        return CODEC;
    }
}
