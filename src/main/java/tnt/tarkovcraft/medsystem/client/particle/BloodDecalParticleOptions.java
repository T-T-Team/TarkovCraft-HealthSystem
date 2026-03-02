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

public record BloodDecalParticleOptions(ParticleType<BloodDecalParticleOptions> type, Direction attachDirection, BlockPos position, int color) implements DecalParticleOptions {

    public BloodDecalParticleOptions(Supplier<ParticleType<BloodDecalParticleOptions>> type, Direction attachDirection, BlockPos position, int color) {
        this(type.get(), attachDirection, position, color);
    }

    public static MapCodec<BloodDecalParticleOptions> codec(ParticleType<BloodDecalParticleOptions> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Direction.CODEC.fieldOf("attachDirection").forGetter(DecalParticleOptions::attachDirection),
                BlockPos.CODEC.fieldOf("position").forGetter(DecalParticleOptions::position),
                Codec.INT.fieldOf("color").forGetter(BloodDecalParticleOptions::color)
        ).apply(instance, (direction, blockPos, integer) -> new BloodDecalParticleOptions(type, direction, blockPos, integer)));
    }

    public static StreamCodec<ByteBuf, BloodDecalParticleOptions> streamCodec(ParticleType<BloodDecalParticleOptions> type) {
        return StreamCodec.composite(
                Direction.STREAM_CODEC, BloodDecalParticleOptions::attachDirection,
                BlockPos.STREAM_CODEC, BloodDecalParticleOptions::position,
                ByteBufCodecs.INT, BloodDecalParticleOptions::color,
                (direction, blockPos, integer) -> new BloodDecalParticleOptions(type, direction, blockPos, integer)
        );
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }
}
