package tnt.tarkovcraft.medsystem.common.blood_system.assignment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import tnt.tarkovcraft.medsystem.util.MedsystemCodecs;

import java.util.Collections;
import java.util.List;

public record UnconsciousModeSettings(boolean enabled, boolean downedOnDeath, boolean hasCustomPose, Vector2fc dimensions, List<String> animationFields) {

    public static final Vector2f DEFAULT_DIMENSIONS = new Vector2f(1.0f, 1.0f);
    public static final Codec<UnconsciousModeSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(UnconsciousModeSettings::enabled),
            Codec.BOOL.optionalFieldOf("downed_on_death", false).forGetter(UnconsciousModeSettings::downedOnDeath),
            Codec.BOOL.optionalFieldOf("custom_pose", false).forGetter(UnconsciousModeSettings::hasCustomPose),
            MedsystemCodecs.VECTOR2F.optionalFieldOf("unconscious_dimensions", DEFAULT_DIMENSIONS).forGetter(UnconsciousModeSettings::dimensions),
            Codec.STRING.listOf().optionalFieldOf("animation_fields", Collections.emptyList()).forGetter(UnconsciousModeSettings::animationFields)
    ).apply(instance, UnconsciousModeSettings::new));

    public static final UnconsciousModeSettings DEFAULT = new UnconsciousModeSettings(false, false, false, DEFAULT_DIMENSIONS, Collections.emptyList());
}
