package tnt.tarkovcraft.medsystem.common.damage.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.function.Function;
import java.util.function.Predicate;

public interface DamageCondition extends Predicate<HitCalculationContext> {

    Codec<DamageCondition> CODEC = MedSystemRegistries.DAMAGE_CONDITIONS.byNameCodec()
            .dispatch(DamageCondition::codec, Function.identity());

    MapCodec<? extends DamageCondition> codec();
}
