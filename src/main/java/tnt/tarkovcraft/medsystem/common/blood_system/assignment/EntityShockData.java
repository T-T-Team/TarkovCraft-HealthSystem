package tnt.tarkovcraft.medsystem.common.blood_system.assignment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.Codecs;

public record EntityShockData(float receptionMultiplier, float recoveryRate, float inShockRecoveryMultiplier, float unconsciousThreshold) {

    public static final EntityShockData DEFAULT = new EntityShockData(1.0F, 0.0005F, 0.1F, 0.75F);

    public static final Codec<EntityShockData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("reception_multiplier", DEFAULT.receptionMultiplier).forGetter(EntityShockData::receptionMultiplier),
            Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("recovery_rate", DEFAULT.recoveryRate).forGetter(EntityShockData::recoveryRate),
            Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("in_shock_recovery_multiplier", DEFAULT.inShockRecoveryMultiplier).forGetter(EntityShockData::inShockRecoveryMultiplier),
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("unconscious_threshold", DEFAULT.unconsciousThreshold).forGetter(EntityShockData::unconsciousThreshold)
    ).apply(instance, EntityShockData::new));

    public boolean isUnconscious(float value) {
        return value >= this.unconsciousThreshold;
    }
}
