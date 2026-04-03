package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventConditions;

public record FallEventCondition(float minFallDistance, float modifier) implements HealthEventCondition {

    public static final MapCodec<FallEventCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("min_fall_distance", 0.0F).forGetter(FallEventCondition::minFallDistance),
            Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("modifier", 1.0F).forGetter(FallEventCondition::modifier)
    ).apply(instance, FallEventCondition::new));

    // chance = (fallDist - minFallDist)^2 * modifier
    @Override
    public HealthEventResult test(HealthEventContext ctx) {
        LivingEntity entity = ctx.getEntity();
        float baseDistance = (float) entity.fallDistance - this.minFallDistance;
        if (baseDistance <= 0) {
            return HealthEventResult.FAILED;
        }
        RandomSource random = entity.getRandom();
        float chance = (baseDistance * baseDistance) * this.modifier;
        return HealthEventResult.condition(random.nextFloat() < chance);
    }

    @Override
    public HealthEventConditionType<?> getType() {
        return MedSystemHealthEventConditions.FALL_FRACTURE.value();
    }
}
