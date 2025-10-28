package tnt.tarkovcraft.medsystem.util;

import com.mojang.serialization.Codec;
import net.minecraft.Util;
import net.minecraft.world.phys.Vec2;

import java.util.List;

public class MedsystemCodecs {

    public static final Codec<Float> NON_NEGATIVE_FLOAT = Codec.floatRange(0.0F, Float.MAX_VALUE);
    public static final Codec<Vec2> VEC2_CODEC = Codec.FLOAT
            .listOf()
            .comapFlatMap(
                    p_405400_ -> Util.fixedSize(p_405400_, 2).map(p_405576_ -> new Vec2(p_405576_.get(0), p_405576_.get(1))),
                    p_405485_ -> List.of(p_405485_.x, p_405485_.y)
            );
}
