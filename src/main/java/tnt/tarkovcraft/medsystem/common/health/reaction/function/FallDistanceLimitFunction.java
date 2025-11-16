package tnt.tarkovcraft.medsystem.common.health.reaction.function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemChanceFunctions;

import javax.annotation.Nullable;

public class FallDistanceLimitFunction implements ChanceFunction {

    public static final MapCodec<FallDistanceLimitFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberProviderType.complexCodecNoDuration(ExtraCodecs.POSITIVE_FLOAT).optionalFieldOf("minDistance", Either.right(0.0F)).forGetter(t -> Either.left(t.minDistance)),
            NumberProviderType.complexCodecNoDuration(ExtraCodecs.POSITIVE_FLOAT).optionalFieldOf("maxDistance", Either.right(Float.MAX_VALUE)).forGetter(t -> Either.left(t.maxDistance))
    ).apply(instance, FallDistanceLimitFunction::new));

    private final NumberProvider minDistance;
    private final NumberProvider maxDistance;

    public FallDistanceLimitFunction(Either<NumberProvider, Float> minDistance, Either<NumberProvider, Float> maxDistance) {
        this.minDistance = NumberProviderType.resolveNoDuration(minDistance);
        this.maxDistance = NumberProviderType.resolveNoDuration(maxDistance);
    }

    @Override
    public float apply(float chance, HealthContainer container, LivingEntity entity, @Nullable DamageSource source, Limb limb) {
        double fallDistance = entity.fallDistance;
        float min = this.minDistance.floatValue();
        float max = this.maxDistance.floatValue();
        if (fallDistance >= min && fallDistance <= max) {
            return chance;
        }
        return 0.0F;
    }

    @Override
    public ChanceFunctionType<?> getType() {
        return MedSystemChanceFunctions.FALL_DISTANCE_LIMIT.value();
    }
}
