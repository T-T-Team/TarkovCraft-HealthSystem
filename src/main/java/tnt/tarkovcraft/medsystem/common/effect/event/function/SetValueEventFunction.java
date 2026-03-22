package tnt.tarkovcraft.medsystem.common.effect.event.function;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventFunctions;

public record SetValueEventFunction(NumberProvider value) implements StatusEffectEventFunction {

    public static final MapCodec<SetValueEventFunction> CODEC = NumberProviderType.CODEC
            .xmap(SetValueEventFunction::new, SetValueEventFunction::value).fieldOf("value");

    @Override
    public float apply(float value, StatusEffectEventContext ctx) {
        return this.value.floatValue();
    }

    @Override
    public StatusEffectEventFunctionType<?> getType() {
        return MedSystemStatusEffectEventFunctions.SET_VALUE.value();
    }
}
