package tnt.tarkovcraft.medsystem.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import tnt.tarkovcraft.medsystem.common.init.MedSystemParticleTypes;

import java.util.function.Supplier;

public record BloodDripParticleOptions(ParticleType<BloodDripParticleOptions> type, int color, int decalAge) implements ParticleOptions {

    public BloodDripParticleOptions(Supplier<ParticleType<BloodDripParticleOptions>> type, int color, int decalAge) {
        this(type.get(), color, decalAge);
    }

    public BloodDripParticleOptions(int color, int decalAge) {
        this(MedSystemParticleTypes.BLOOD_DRIP, color, decalAge);
    }

    public BloodDripParticleOptions(int color) {
        this(color, 0);
    }

    public static MapCodec<BloodDripParticleOptions> codec(ParticleType<BloodDripParticleOptions> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.INT.fieldOf("color").forGetter(BloodDripParticleOptions::color),
                Codec.INT.fieldOf("decal_age").forGetter(BloodDripParticleOptions::decalAge)
        ).apply(instance, (color, age) -> new BloodDripParticleOptions(type, color, age)));
    }

    public static StreamCodec<ByteBuf, BloodDripParticleOptions> streamCodec(ParticleType<BloodDripParticleOptions> type) {
        return StreamCodec.composite(
                ByteBufCodecs.INT, BloodDripParticleOptions::color,
                ByteBufCodecs.INT, BloodDripParticleOptions::decalAge,
                (color, age) -> new BloodDripParticleOptions(type, color, age)
        );
    }

    @Override
    public ParticleType<?> getType() {
        return this.type;
    }
}
