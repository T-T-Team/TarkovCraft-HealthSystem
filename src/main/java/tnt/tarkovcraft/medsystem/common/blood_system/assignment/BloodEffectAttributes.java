package tnt.tarkovcraft.medsystem.common.blood_system.assignment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.effect.BloodLossStatusEffect;

import java.util.Collections;
import java.util.Map;

public record BloodEffectAttributes(float painThreshold, float grayscaleStart, float grayscaleEnd, Map<BloodLossStatusEffect.Stage, Float> bloodlossEffect) {

    public static final Codec<BloodEffectAttributes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("pain_threshold", 0.0F).forGetter(BloodEffectAttributes::painThreshold),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("grayscale_start", 1.0F).forGetter(BloodEffectAttributes::grayscaleStart),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("grayscale_end", 0.0F).forGetter(BloodEffectAttributes::grayscaleEnd),
            Codec.unboundedMap(BloodLossStatusEffect.Stage.CODEC, Codec.FLOAT).optionalFieldOf("bloodloss_effect", Collections.emptyMap()).forGetter(BloodEffectAttributes::bloodlossEffect)
    ).apply(instance, BloodEffectAttributes::new));
    public static final BloodEffectAttributes DEFAULT = new BloodEffectAttributes(0.0F, 1.0F, 0.0F, Collections.emptyMap());

    public boolean isInPain(float value) {
        return value < this.painThreshold;
    }

    public boolean shouldApplyGrayscale(float value) {
        return value <= this.grayscaleStart;
    }

    public float getGrayscale(float value) {
        float clampedValue = Mth.clamp(value, this.grayscaleEnd, this.grayscaleStart);
        return 1.0F - ((clampedValue - this.grayscaleEnd) / (this.grayscaleStart - this.grayscaleEnd));
    }

    public BloodLossStatusEffect.@Nullable Stage getBloodLossStage(float percentage) {
        BloodLossStatusEffect.Stage activeStage = null;
        float minBound = 1.0F;
        for (Map.Entry<BloodLossStatusEffect.Stage, Float> entry : this.bloodlossEffect.entrySet()) {
            float bound = entry.getValue();
            if (percentage <= bound && bound < minBound) {
                activeStage = entry.getKey();
                minBound = bound;
            }
        }
        return activeStage;
    }
}
