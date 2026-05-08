package tnt.tarkovcraft.medsystem.common.blood_system.assignment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.Codecs;

public record EntityShockData(float receptionMultiplier, float recoveryRate, float inShockRecoveryMultiplier, float unconsciousThreshold, float unconsciousWakeUpThreshold) {

    public static final EntityShockData DEFAULT = new EntityShockData(1.0F, 0.0005F, 0.1F, 0.75F, 0.15F);

    public static final Codec<EntityShockData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("reception_multiplier", DEFAULT.receptionMultiplier).forGetter(EntityShockData::receptionMultiplier),
            Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("recovery_rate", DEFAULT.recoveryRate).forGetter(EntityShockData::recoveryRate),
            Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("in_shock_recovery_multiplier", DEFAULT.inShockRecoveryMultiplier).forGetter(EntityShockData::inShockRecoveryMultiplier),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("unconscious_threshold", DEFAULT.unconsciousThreshold).forGetter(EntityShockData::unconsciousThreshold),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("unconscious_wake_up_threshold", DEFAULT.unconsciousWakeUpThreshold).forGetter(EntityShockData::unconsciousWakeUpThreshold)
    ).apply(instance, EntityShockData::new));

    public boolean isUnconscious(boolean unconscious, float value) {
        return unconscious
                ? value > this.unconsciousWakeUpThreshold
                : value >= this.unconsciousThreshold;
    }
}
