package tnt.tarkovcraft.medsystem.common.damage_effect.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.NumberOperator;
import tnt.tarkovcraft.core.util.UnitFormat;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectFunctions;

public record CalculateFunction(NumberOperator operator, float value, UnitFormat.RoundingMode rounding) implements DamageEffectFunction {

    public static final MapCodec<CalculateFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberOperator.CODEC.fieldOf("operator").forGetter(CalculateFunction::operator),
            Codec.FLOAT.fieldOf("value").forGetter(CalculateFunction::value),
            UnitFormat.RoundingMode.CODEC.optionalFieldOf("rounding", UnitFormat.RoundingMode.FLOOR).forGetter(CalculateFunction::rounding)
    ).apply(instance, CalculateFunction::new));

    @Override
    public int apply(int value, DamageEffectContext context) {
        return (int) this.rounding.applyAsDouble(this.operator.applyAsDouble(value, this.value));
    }

    @Override
    public DamageEffectFunctionType<?> getType() {
        return MedSystemDamageEffectFunctions.CALCULATION.value();
    }
}
