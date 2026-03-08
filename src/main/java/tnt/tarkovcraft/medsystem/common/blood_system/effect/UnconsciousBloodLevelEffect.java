package tnt.tarkovcraft.medsystem.common.blood_system.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.core.common.util.AttributeNumber;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;
import tnt.tarkovcraft.medsystem.common.init.MedSystemBloodLevelEffects;

public record UnconsciousBloodLevelEffect(NumberProvider duration, AttributeNumber chance, UnconsciousOptions options) implements BloodLevelEffect {

    public static final MapCodec<UnconsciousBloodLevelEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberProviderType.CODEC.fieldOf("duration").forGetter(UnconsciousBloodLevelEffect::duration),
            AttributeNumber.CODEC.optionalFieldOf("chance", AttributeNumber.constant(1.0)).forGetter(UnconsciousBloodLevelEffect::chance),
            UnconsciousOptions.CODEC.fieldOf("options").forGetter(UnconsciousBloodLevelEffect::options)
    ).apply(instance, UnconsciousBloodLevelEffect::new));

    @Override
    public void applyEffects(LivingEntity entity, ServerLevel level, EntityBloodSystem bloodSystem) {
        double chance = this.chance.getValue(entity);
        RandomSource random = entity.getRandom();
        applyUnconsciousMode(bloodSystem, random, (float) chance, this.duration.intValue(), this.options);
    }

    public static void applyUnconsciousMode(EntityBloodSystem bloodSystem, RandomSource random, float chance, int duration, UnconsciousOptions options) {
        if (duration <= 0 || chance <= 0 || bloodSystem.getActiveUnconsciousModeOptions() == UnconsciousOptions.DOWNED)
            return;
        EntityBloodSystemDefinition definition = bloodSystem.getDefinition();
        if (!definition.isUnconsciousModeAllowed())
            return;
        if (chance >= 1.0F || random.nextFloat() < chance) {
            bloodSystem.setOrExtendedUnconscious(duration, options);
        }
    }

    @Override
    public BloodLevelEffectType<?> getType() {
        return MedSystemBloodLevelEffects.UNCONSCIOUS.value();
    }
}
