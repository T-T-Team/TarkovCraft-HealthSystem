package tnt.tarkovcraft.medsystem.common.damage.function;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.health.calc.GenericHitCalculator;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculator;

public class GenericDamageFunction implements DamageFunction {

    public static final MapCodec<GenericDamageFunction> CODEC = MapCodec.unit(GenericDamageFunction::new);

    @Override
    public HitCalculator resolve(HitCalculationContext context) {
        return GenericHitCalculator.INSTANCE;
    }

    @Override
    public MapCodec<? extends DamageFunction> codec() {
        return CODEC;
    }
}
