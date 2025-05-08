package tnt.tarkovcraft.medsystem.common.health.reaction.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;

public record ChanceFunctionType<T extends ChanceFunction>(ResourceLocation identifier, MapCodec<T> codec) {

    public static final Codec<ChanceFunction> CODEC = MedSystemRegistries.CHANCE_FUNCTION.byNameCodec().dispatch(ChanceFunction::getType, ChanceFunctionType::codec);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ChanceFunctionType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
