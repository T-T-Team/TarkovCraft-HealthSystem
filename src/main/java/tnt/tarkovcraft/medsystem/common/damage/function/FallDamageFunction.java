package tnt.tarkovcraft.medsystem.common.damage.function;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.ExtraCodecs;
import tnt.tarkovcraft.medsystem.common.health.calc.FallDamageHitCalculator;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculator;

public record FallDamageFunction(float damageScale) implements DamageFunction {

    public static final MapCodec<FallDamageFunction> CODEC = ExtraCodecs.NON_NEGATIVE_FLOAT
            .xmap(FallDamageFunction::new, FallDamageFunction::damageScale).fieldOf("damage_scale");

    @Override
    public HitCalculator resolve(HitCalculationContext context) {
        return new FallDamageHitCalculator(this.damageScale);
    }

    @Override
    public MapCodec<? extends DamageFunction> codec() {
        return CODEC;
    }
}
