package tnt.tarkovcraft.medsystem.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.Util;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec2;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import tnt.tarkovcraft.core.util.helper.ARGB;

import java.util.HexFormat;
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
    public static final Codec<Integer> RGB_COLOR_CODEC = Codec.withAlternative(
            Codec.INT,
            ExtraCodecs.VECTOR3F,
            p_454448_ -> ARGB.colorFromFloat(1.0F, p_454448_.x(), p_454448_.y(), p_454448_.z())
    );
    public static final Codec<Integer> STRING_RGB_COLOR = Codec.withAlternative(
            hexColor(6).xmap(ARGB::opaque, ARGB::transparent),
            RGB_COLOR_CODEC
    );
    public static final Codec<Vector2fc> VECTOR2F = Codec.FLOAT
            .listOf()
            .comapFlatMap(
                    floats -> Util.fixedSize(floats, 2).map(list -> new Vector2f(list.get(0), list.get(1))),
                    vector2fc -> List.of(vector2fc.x(), vector2fc.y())
            );

    private static Codec<Integer> hexColor(int digits) {
        long limit = (1L << digits * 4) - 1L;
        return Codec.STRING.comapFlatMap(p_457388_ -> {
            if (!p_457388_.startsWith("#")) {
                return DataResult.error(() -> "Hex color must begin with #");
            } else {
                int j = p_457388_.length() - "#".length();
                if (j != digits) {
                    return DataResult.error(() -> "Hex color is wrong size, expected " + digits + " digits but got " + j);
                } else {
                    try {
                        long k = HexFormat.fromHexDigitsToLong(p_457388_, "#".length(), p_457388_.length());
                        return k >= 0L && k <= limit ? DataResult.success((int)k) : DataResult.error(() -> "Color value out of range: " + p_457388_);
                    } catch (NumberFormatException numberformatexception) {
                        return DataResult.error(() -> "Invalid color value: " + p_457388_);
                    }
                }
            }
        }, p_457393_ -> "#" + HexFormat.of().toHexDigits(p_457393_, digits));
    }
}
