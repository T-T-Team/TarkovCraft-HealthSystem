package tnt.tarkovcraft.medsystem.common.blood_system.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.util.AttributeNumber;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.config.TimeRange;

public record ConfigurableUnconsciousBloodLevelEffect(AttributeNumber chance, UnconsciousOptions options) implements BloodLevelEffect {

    public static final MapCodec<ConfigurableUnconsciousBloodLevelEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            AttributeNumber.CODEC.optionalFieldOf("chance", AttributeNumber.constant(1.0)).forGetter(ConfigurableUnconsciousBloodLevelEffect::chance),
            UnconsciousOptions.CODEC.fieldOf("options").forGetter(ConfigurableUnconsciousBloodLevelEffect::options)
    ).apply(instance, ConfigurableUnconsciousBloodLevelEffect::new));

    @Override
    public void applyEffects(LivingEntity entity, ServerLevel level, EntityBloodSystem bloodSystem) {
        MedSystemConfig config = MedicalSystem.getConfig();
        TimeRange range = config.bloodSystem.unconsciousOnBloodLoss;
        RandomSource random = entity.getRandom();
        int duration = range.getDurationInSeconds(random);
        float chance = (float) this.chance.getValue(entity);
        UnconsciousBloodLevelEffect.applyUnconsciousMode(bloodSystem, entity, random, chance, duration, this.options);
    }

    @Override
    public MapCodec<? extends BloodLevelEffect> codec() {
        return CODEC;
    }
}
