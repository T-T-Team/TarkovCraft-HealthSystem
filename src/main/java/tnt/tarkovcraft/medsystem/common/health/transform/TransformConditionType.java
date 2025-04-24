package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;

public record TransformConditionType<C extends TransformCondition>(ResourceLocation identifier, MapCodec<C> codec) {

    public static final Codec<TransformCondition> CODEC = MedSystemRegistries.TRANSFORM_CONDITION.byNameCodec().dispatch(TransformCondition::getType, TransformConditionType::codec);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof TransformConditionType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
