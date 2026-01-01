package tnt.tarkovcraft.medsystem.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.Util;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec2;

import java.util.List;
import java.util.Locale;

public class MedsystemCodecs {

    public static final Codec<Float> NON_NEGATIVE_FLOAT = Codec.floatRange(0.0F, Float.MAX_VALUE);
    public static final Codec<Vec2> VEC2_CODEC = Codec.FLOAT
            .listOf()
            .comapFlatMap(
                    p_405400_ -> Util.fixedSize(p_405400_, 2).map(p_405576_ -> new Vec2(p_405576_.get(0), p_405576_.get(1))),
                    p_405485_ -> List.of(p_405485_.x, p_405485_.y)
            );
    public static final Codec<Pose> POSE_CODEC = Codec.STRING.comapFlatMap(
            key -> {
                try {
                    return DataResult.success(Pose.valueOf(key.toUpperCase(Locale.ROOT)));
                } catch (Exception e) {
                    return DataResult.error(() -> "Invalid pose: " + key);
                }
            },
            pose -> pose.name().toLowerCase(Locale.ROOT)
    );
}
