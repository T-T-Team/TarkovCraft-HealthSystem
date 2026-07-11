package tnt.tarkovcraft.medsystem.common.damage.function;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculator;
import tnt.tarkovcraft.medsystem.common.health.calc.MovementDamageHitCalculator;

public final class BrokenLimbDamageFunction implements DamageFunction {

    public static final MapCodec<BrokenLimbDamageFunction> CODEC = MapCodec.unit(BrokenLimbDamageFunction::new);

    @Override
    public HitCalculator resolve(HitCalculationContext context) {
        return MovementDamageHitCalculator.INSTANCE;
    }

    @Override
    public MapCodec<? extends DamageFunction> codec() {
        return CODEC;
    }
}
