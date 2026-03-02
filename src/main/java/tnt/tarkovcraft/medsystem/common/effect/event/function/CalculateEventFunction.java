package tnt.tarkovcraft.medsystem.common.effect.event.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.NumberFormatter;
import tnt.tarkovcraft.core.util.NumberOperator;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventFunctions;

public record CalculateEventFunction(NumberOperator operator, float value, NumberFormatter.RoundingMode rounding) implements StatusEffectEventFunction {

    public static final MapCodec<CalculateEventFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberOperator.CODEC.fieldOf("operator").forGetter(CalculateEventFunction::operator),
            Codec.FLOAT.fieldOf("value").forGetter(CalculateEventFunction::value),
            NumberFormatter.RoundingMode.CODEC.optionalFieldOf("rounding", NumberFormatter.RoundingMode.FLOOR).forGetter(CalculateEventFunction::rounding)
    ).apply(instance, CalculateEventFunction::new));

    @Override
    public int apply(int value, StatusEffectEventContext context) {
        return (int) this.rounding.applyAsDouble(this.operator.applyAsDouble(value, this.value));
    }

    @Override
    public StatusEffectEventFunctionType<?> getType() {
        return MedSystemStatusEffectEventFunctions.CALCULATION.value();
    }
}
