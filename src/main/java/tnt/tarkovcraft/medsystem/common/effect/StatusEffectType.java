package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record StatusEffectType<S extends StatusEffect>(ResourceLocation identifier, MapCodec<S> codec) {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StatusEffectType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
