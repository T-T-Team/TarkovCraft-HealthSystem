package tnt.tarkovcraft.medsystem.common.health_event.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Collection;
import java.util.Objects;

public record HealthEventFunctionType<F extends HealthEventFunction>(Identifier identifier, MapCodec<F> codec) {

    public static final Codec<HealthEventFunction> CODEC = MedSystemRegistries.HEALTH_EVENT_FUNCTION.byNameCodec()
            .dispatch(HealthEventFunction::getType, HealthEventFunctionType::codec);

    public static float applyFunctions(final float initialValue, HealthEventContext context, Collection<HealthEventFunction> functions) {
        float value = initialValue;
        for (HealthEventFunction function : functions) {
            value = function.apply(value, context);
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HealthEventFunctionType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
