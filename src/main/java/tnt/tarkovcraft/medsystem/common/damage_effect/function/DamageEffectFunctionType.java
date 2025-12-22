package tnt.tarkovcraft.medsystem.common.damage_effect.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Collection;
import java.util.Objects;

public record DamageEffectFunctionType<F extends DamageEffectFunction>(ResourceLocation identifier, MapCodec<F> codec) {

    public static final Codec<DamageEffectFunction> CODEC = MedSystemRegistries.DAMAGE_EFFECT_FUNCTION.byNameCodec().dispatch(DamageEffectFunction::getType, DamageEffectFunctionType::codec);

    public static int applyFunctions(final int initialValue, DamageEffectContext context, Collection<DamageEffectFunction> functions) {
        int value = initialValue;
        for (DamageEffectFunction function : functions) {
            value = function.apply(value, context);
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DamageEffectFunctionType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
