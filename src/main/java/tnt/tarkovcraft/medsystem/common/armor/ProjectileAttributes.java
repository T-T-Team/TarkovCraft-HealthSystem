package tnt.tarkovcraft.medsystem.common.armor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ProjectileAttributes(float massFactor) {

    public static final ProjectileAttributes NONE = new ProjectileAttributes(0.0F);

    public static final MapCodec<ProjectileAttributes> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("massFactor").forGetter(ProjectileAttributes::massFactor)
    ).apply(instance, ProjectileAttributes::new));
    public static final StreamCodec<ByteBuf, ProjectileAttributes> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ProjectileAttributes::massFactor,
            ProjectileAttributes::new
    );

    public static ProjectileAttributes none() {
        return NONE;
    }
}
