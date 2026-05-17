package tnt.tarkovcraft.medsystem.common.consume_effect;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record ConsumeEffectType<C extends ConsumeEffect>(ResourceLocation identifier, MapCodec<C> codec) {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ConsumeEffectType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }

    @Override
    public String toString() {
        return this.identifier.toString();
    }
}
