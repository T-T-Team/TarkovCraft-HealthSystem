package tnt.tarkovcraft.medsystem.common.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record HealTarget(boolean self, int entityId, String limbCode) {

    public static final Codec<HealTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("self", true).forGetter(HealTarget::self),
            Codec.INT.optionalFieldOf("entityId", 0).forGetter(HealTarget::entityId),
            Codec.STRING.fieldOf("limb").forGetter(HealTarget::limbCode)
    ).apply(instance, HealTarget::new));
    public static final StreamCodec<ByteBuf, HealTarget> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, HealTarget::self,
            ByteBufCodecs.INT, HealTarget::entityId,
            ByteBufCodecs.STRING_UTF8, HealTarget::limbCode,
            HealTarget::new
    );
}
