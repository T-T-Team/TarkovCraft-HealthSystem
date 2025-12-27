package tnt.tarkovcraft.medsystem.common.health.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;

public record StateFilterType<F extends StateFilter>(Identifier identifier, MapCodec<F> codec) {

    public static final Codec<StateFilter> CODEC = MedSystemRegistries.STATE_FILTER.byNameCodec().dispatch(StateFilter::getType, StateFilterType::codec);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof StateFilterType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
