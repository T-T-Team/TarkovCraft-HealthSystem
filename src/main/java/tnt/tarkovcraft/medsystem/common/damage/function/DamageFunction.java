package tnt.tarkovcraft.medsystem.common.damage.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculator;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.function.Function;

public interface DamageFunction {

    Codec<DamageFunction> CODEC = MedSystemRegistries.DAMAGE_FUNCTIONS.byNameCodec()
            .dispatch("function", DamageFunction::codec, Function.identity());

    HitCalculator resolve(HitCalculationContext context);

    MapCodec<? extends DamageFunction> codec();
}
