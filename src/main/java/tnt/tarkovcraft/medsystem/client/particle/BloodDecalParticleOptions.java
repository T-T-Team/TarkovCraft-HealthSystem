package tnt.tarkovcraft.medsystem.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import tnt.tarkovcraft.core.client.particle.DecalParticleOptions;

import java.util.function.Supplier;

public record BloodDecalParticleOptions(ParticleType<BloodDecalParticleOptions> type, Direction attachDirection, BlockPos position, int color, int decalAge) implements DecalParticleOptions {

    public BloodDecalParticleOptions(Supplier<ParticleType<BloodDecalParticleOptions>> type, Direction attachDirection, BlockPos position, int color, int decalAge) {
        this(type.get(), attachDirection, position, color, decalAge);
    }

    public BloodDecalParticleOptions(Supplier<ParticleType<BloodDecalParticleOptions>> type, Direction attachDirection, BlockPos position, int color) {
        this(type.get(), attachDirection, position, color, 0);
    }

    public static MapCodec<BloodDecalParticleOptions> codec(ParticleType<BloodDecalParticleOptions> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Direction.CODEC.fieldOf("attachDirection").forGetter(DecalParticleOptions::attachDirection),
                BlockPos.CODEC.fieldOf("position").forGetter(DecalParticleOptions::position),
                Codec.INT.fieldOf("color").forGetter(BloodDecalParticleOptions::color),
                Codec.INT.fieldOf("decal_age").forGetter(BloodDecalParticleOptions::decalAge)
        ).apply(instance, (direction, blockPos, color, age) -> new BloodDecalParticleOptions(type, direction, blockPos, color, age)));
    }

    public static StreamCodec<ByteBuf, BloodDecalParticleOptions> streamCodec(ParticleType<BloodDecalParticleOptions> type) {
        return StreamCodec.composite(
                Direction.STREAM_CODEC, BloodDecalParticleOptions::attachDirection,
                BlockPos.STREAM_CODEC, BloodDecalParticleOptions::position,
                ByteBufCodecs.INT, BloodDecalParticleOptions::color,
                ByteBufCodecs.INT, BloodDecalParticleOptions::decalAge,
                (direction, blockPos, color, age) -> new BloodDecalParticleOptions(type, direction, blockPos, color, age)
        );
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }
}
