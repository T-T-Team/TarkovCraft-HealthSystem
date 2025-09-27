package tnt.tarkovcraft.medsystem.common.status;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public record BloodContainer(float capacity, float value, boolean refillable) {

    public static final Codec<BloodContainer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("capacity").forGetter(BloodContainer::capacity),
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("value", 0.0F).forGetter(BloodContainer::value),
            Codec.BOOL.optionalFieldOf("refillable", true).forGetter(BloodContainer::refillable)
    ).apply(instance, BloodContainer::new));
    public static final StreamCodec<ByteBuf, BloodContainer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, BloodContainer::capacity,
            ByteBufCodecs.FLOAT, BloodContainer::value,
            ByteBufCodecs.BOOL, BloodContainer::refillable,
            BloodContainer::new
    );

    public boolean isEmpty() {
        return this.value <= 0;
    }

    public boolean isFull() {
        return this.value >= this.capacity;
    }

    public float getMissingCapacity() {
        return this.capacity - this.value;
    }

    public BloodContainer fill(float amount) {
        float newAmount = Mth.clamp(this.value + amount, 0.0F, this.capacity);
        return new BloodContainer(this.capacity, newAmount, this.refillable);
    }

    public BloodContainer extract(float amount) {
        float newAmount = Mth.clamp(this.value - amount, 0.0F, this.capacity);
        return new BloodContainer(this.capacity, newAmount, this.refillable);
    }
}
