package tnt.tarkovcraft.medsystem.common.damage_effect.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffect;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContextType;

public record FallDamageEffectCondition(float minFallDistance, float modifier) implements DamageEffectCondition {

    public static final MapCodec<FallDamageEffectCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("min_fall_distance", 0.0F).forGetter(FallDamageEffectCondition::minFallDistance),
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("modifier", 1.0F).forGetter(FallDamageEffectCondition::modifier)
    ).apply(instance, FallDamageEffectCondition::new));

    @Override
    public boolean matches(DamageEffectContext context) {
        LivingEntity entity = context.target();
        float baseDistance = (float) entity.fallDistance - this.minFallDistance;
        if (baseDistance <= 0) {
            return false;
        }
        RandomSource random = entity.getRandom();
        float chance = (baseDistance * baseDistance) * this.modifier;
        return random.nextFloat() < chance;
    }

    @Override
    public DamageEffectConditionType<?> getType() {
        return null;
    }

    @Override
    public void validate(DamageEffectContextType contextType) {
        DamageEffect.validateContext(this, contextType, DamageEffectContextType.ON_HURT);
    }
}
