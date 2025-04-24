package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Objects;

public record EntityHitboxTransformType<T extends EntityHitboxTransform>(ResourceLocation identifier, MapCodec<T> codec) {

    public static final Codec<EntityHitboxTransform> CODEC = MedSystemRegistries.TRANSFORM.byNameCodec().dispatch(EntityHitboxTransform::getType, EntityHitboxTransformType::codec);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EntityHitboxTransformType<?> that)) return false;
        return Objects.equals(identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(identifier);
    }
}
