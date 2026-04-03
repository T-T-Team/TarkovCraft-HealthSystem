package tnt.tarkovcraft.medsystem.common.health_event.function;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventFunctions;

public record SetValueEventFunction(NumberProvider value) implements HealthEventFunction {

    public static final MapCodec<SetValueEventFunction> CODEC = NumberProviderType.CODEC
            .xmap(SetValueEventFunction::new, SetValueEventFunction::value).fieldOf("value");

    @Override
    public float apply(float value, HealthEventContext ctx) {
        return this.value.floatValue();
    }

    @Override
    public HealthEventFunctionType<?> getType() {
        return MedSystemHealthEventFunctions.SET_VALUE.value();
    }
}
