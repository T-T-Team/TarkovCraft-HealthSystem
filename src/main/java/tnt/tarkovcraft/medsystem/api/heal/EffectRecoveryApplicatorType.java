package tnt.tarkovcraft.medsystem.api.heal;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Objects;

public record EffectRecoveryApplicatorType<A extends EffectRecoveryApplicator>(ResourceLocation identifier, MapCodec<A> codec) {

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EffectRecoveryApplicatorType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }

    @Override
    public @Nonnull String toString() {
        return this.identifier.toString();
    }
}
