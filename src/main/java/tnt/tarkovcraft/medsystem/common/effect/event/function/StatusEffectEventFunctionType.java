package tnt.tarkovcraft.medsystem.common.effect.event.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Collection;
import java.util.Objects;

public record StatusEffectEventFunctionType<F extends StatusEffectEventFunction>(ResourceLocation identifier, MapCodec<F> codec) {

    public static final Codec<StatusEffectEventFunction> CODEC = MedSystemRegistries.STATUS_EFFECT_EVENT_FUNCTION.byNameCodec()
            .dispatch(StatusEffectEventFunction::getType, StatusEffectEventFunctionType::codec);

    public static int applyFunctions(final int initialValue, StatusEffectEventContext context, Collection<StatusEffectEventFunction> functions) {
        int value = initialValue;
        for (StatusEffectEventFunction function : functions) {
            value = function.apply(value, context);
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StatusEffectEventFunctionType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
