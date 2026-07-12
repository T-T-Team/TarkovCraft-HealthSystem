package tnt.tarkovcraft.medsystem.common.blood_system;

import java.util.Map;

public record UnconsciousAnimationState(float collapseProgress, Map<String, Float> animationMetadata) {

    public static final UnconsciousAnimationState DEFAULT_STATE = new UnconsciousAnimationState(1.0F, Map.of());

    public float getMetadataValue(String key) {
        return this.animationMetadata.getOrDefault(key, 1.0F);
    }
}
