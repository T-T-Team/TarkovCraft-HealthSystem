package tnt.tarkovcraft.medsystem.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import tnt.tarkovcraft.medsystem.common.init.MedSystemParticleTypes;

import java.util.function.Supplier;

public record BloodDripParticleOptions(ParticleType<BloodDripParticleOptions> type, int color) implements ParticleOptions {

    public BloodDripParticleOptions(Supplier<ParticleType<BloodDripParticleOptions>> type, int color) {
        this(type.get(), color);
    }

    public BloodDripParticleOptions(int color) {
        this(MedSystemParticleTypes.BLOOD_DRIP, color);
    }

    public static MapCodec<BloodDripParticleOptions> codec(ParticleType<BloodDripParticleOptions> type) {
        return Codec.INT.xmap(integer -> new BloodDripParticleOptions(type, integer), BloodDripParticleOptions::color)
                .fieldOf("color");
    }

    public static StreamCodec<ByteBuf, BloodDripParticleOptions> streamCodec(ParticleType<BloodDripParticleOptions> type) {
        return ByteBufCodecs.INT.map(integer -> new BloodDripParticleOptions(type, integer), BloodDripParticleOptions::color);
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }
}
