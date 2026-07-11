package tnt.tarkovcraft.medsystem.common.damage.function;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculator;
import tnt.tarkovcraft.medsystem.common.health.calc.InLiquidDamageCalculator;

public record InLiquidDamageFunction(float damageScale) implements DamageFunction {

    public static final MapCodec<InLiquidDamageFunction> CODEC = Codecs.NON_NEGATIVE_FLOAT
            .xmap(InLiquidDamageFunction::new, InLiquidDamageFunction::damageScale).fieldOf("damage_scale");

    @Override
    public HitCalculator resolve(HitCalculationContext context) {
        return new InLiquidDamageCalculator(this.damageScale);
    }

    @Override
    public MapCodec<? extends DamageFunction> codec() {
        return CODEC;
    }
}
