package tnt.tarkovcraft.medsystem.common.health.reaction.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.common.init.MedSystemChanceFunctions;

public class FallDistanceScaleFunction implements ChanceFunction {

    public static final MapCodec<FallDistanceScaleFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(t -> t.scale)
    ).apply(instance, FallDistanceScaleFunction::new));

    private final float scale;

    public FallDistanceScaleFunction(float scale) {
        this.scale = scale;
    }

    @Override
    public float apply(float chance, Context context) {
        return context.get(ContextKeys.LIVING_ENTITY).map(entity -> {
            double distance = entity.fallDistance;
            return (float) (distance * scale) * chance;
        }).orElse(chance);
    }

    @Override
    public ChanceFunctionType<?> getType() {
        return MedSystemChanceFunctions.FALL_DISTANCE.get();
    }
}
