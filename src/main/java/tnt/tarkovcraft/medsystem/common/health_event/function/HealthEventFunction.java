package tnt.tarkovcraft.medsystem.common.health_event.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Collection;
import java.util.function.Function;

public interface HealthEventFunction {

    Codec<HealthEventFunction> CODEC = MedSystemRegistries.HEALTH_EVENT_FUNCTION.byNameCodec()
            .dispatch(HealthEventFunction::codec, Function.identity());

    static float applyFunctions(final float initialValue, HealthEventContext context, Collection<HealthEventFunction> functions) {
        float value = initialValue;
        for (HealthEventFunction function : functions) {
            value = function.apply(value, context);
        }
        return value;
    }

    float apply(float value, HealthEventContext ctx);

    MapCodec<? extends HealthEventFunction> codec();
}
