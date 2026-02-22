package tnt.tarkovcraft.medsystem.common.effect.event.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.TriggerResult;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventConditions;

public record FallStatusEffectEventCondition(float minFallDistance, float modifier) implements StatusEffectEventCondition {

    public static final MapCodec<FallStatusEffectEventCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("min_fall_distance", 0.0F).forGetter(FallStatusEffectEventCondition::minFallDistance),
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("modifier", 1.0F).forGetter(FallStatusEffectEventCondition::modifier)
    ).apply(instance, FallStatusEffectEventCondition::new));

    // chance = (fallDist - minFallDist)^2 * modifier
    @Override
    public TriggerResult test(StatusEffectEventContext ctx) {
        LivingEntity entity = ctx.getEntity();
        float baseDistance = (float) entity.fallDistance - this.minFallDistance;
        if (baseDistance <= 0) {
            return TriggerResult.FAILED;
        }
        RandomSource random = entity.getRandom();
        float chance = (baseDistance * baseDistance) * this.modifier;
        return TriggerResult.condition(random.nextFloat() < chance);
    }

    @Override
    public StatusEffectEventConditionType<?> getType() {
        return MedSystemStatusEffectEventConditions.FALL_FRACTURE.value();
    }
}
