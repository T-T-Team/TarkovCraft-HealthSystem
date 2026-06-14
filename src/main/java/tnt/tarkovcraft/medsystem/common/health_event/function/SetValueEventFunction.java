package tnt.tarkovcraft.medsystem.common.health_event.function;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;

public record SetValueEventFunction(float value) implements HealthEventFunction {

    public static final MapCodec<SetValueEventFunction> CODEC = NumberProvider.FLOAT
            .xmap(SetValueEventFunction::new, SetValueEventFunction::value).fieldOf("value");

    @Override
    public float apply(float value, HealthEventContext ctx) {
        return this.value;
    }

    @Override
    public MapCodec<? extends HealthEventFunction> codec() {
        return CODEC;
    }
}
