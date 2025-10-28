package tnt.tarkovcraft.medsystem.common.health.reaction.function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemChanceFunctions;

import javax.annotation.Nullable;

public class FallDistanceScaleFunction implements ChanceFunction {

    public static final MapCodec<FallDistanceScaleFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberProviderType.complexCodecNoDuration(Codec.FLOAT).optionalFieldOf("scale", Either.right(1.0F)).forGetter(t -> Either.left(t.scale))
    ).apply(instance, FallDistanceScaleFunction::new));

    private final NumberProvider scale;

    public FallDistanceScaleFunction(Either<NumberProvider, Float> scale) {
        this.scale = NumberProviderType.resolveNoDuration(scale);
    }

    @Override
    public float apply(float chance, HealthContainer container, LivingEntity entity, @Nullable DamageSource source, Limb limb) {
        double distance = entity.fallDistance;
        float scaleValue = this.scale.floatValue();
        return (float) (distance * scaleValue) * chance;
    }

    @Override
    public ChanceFunctionType<?> getType() {
        return MedSystemChanceFunctions.FALL_DISTANCE.value();
    }
}
