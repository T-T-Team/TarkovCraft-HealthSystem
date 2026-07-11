package tnt.tarkovcraft.medsystem.common.damage.function;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.health.calc.DelegateHitCalculator;
import tnt.tarkovcraft.medsystem.common.health.calc.GenericHitCalculator;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculator;
import tnt.tarkovcraft.medsystem.common.health.distributor.PoisonDamageDistributor;

public final class PoisonDamageFunction implements DamageFunction {

    public static final MapCodec<PoisonDamageFunction> CODEC = MapCodec.unit(PoisonDamageFunction::new);

    @Override
    public HitCalculator resolve(HitCalculationContext context) {
        return new DelegateHitCalculator(GenericHitCalculator.INSTANCE, PoisonDamageDistributor.INSTANCE);
    }

    @Override
    public MapCodec<? extends DamageFunction> codec() {
        return CODEC;
    }
}
