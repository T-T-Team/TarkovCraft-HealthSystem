package tnt.tarkovcraft.medsystem.common.health_event.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.NumberFormatter;
import tnt.tarkovcraft.core.util.NumberOperator;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;

public record CalculateEventFunction(NumberOperator operator, float value, NumberFormatter.RoundingMode rounding) implements HealthEventFunction {

    public static final MapCodec<CalculateEventFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberOperator.CODEC.fieldOf("operator").forGetter(CalculateEventFunction::operator),
            Codec.FLOAT.fieldOf("value").forGetter(CalculateEventFunction::value),
            NumberFormatter.RoundingMode.CODEC.optionalFieldOf("rounding", NumberFormatter.RoundingMode.FLOOR).forGetter(CalculateEventFunction::rounding)
    ).apply(instance, CalculateEventFunction::new));

    @Override
    public float apply(float value, HealthEventContext context) {
        return (float) this.rounding.applyAsDouble(this.operator.applyAsDouble(value, this.value));
    }

    @Override
    public MapCodec<? extends HealthEventFunction> codec() {
        return CODEC;
    }
}
