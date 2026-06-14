package tnt.tarkovcraft.medsystem.common.health_event.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;

import java.util.Collections;
import java.util.Map;

public record LimbTypeScaleEventFunction(Map<LimbType, Float> limbTypeScales, float scale) implements HealthEventFunction {

    public static final MapCodec<LimbTypeScaleEventFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.unboundedMap(LimbType.CODEC, NumberProvider.FLOAT).optionalFieldOf("limb_scaling", Collections.emptyMap()).forGetter(LimbTypeScaleEventFunction::limbTypeScales),
            NumberProvider.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(LimbTypeScaleEventFunction::scale)
    ).apply(instance, LimbTypeScaleEventFunction::new));

    @Override
    public float apply(float value, HealthEventContext ctx) {
        Limb limb = ctx.getLimb();
        LimbType limbType = limb.getType();
        float limbScale = this.limbTypeScales.getOrDefault(limbType, this.scale);
        return value * limbScale;
    }

    @Override
    public MapCodec<? extends HealthEventFunction> codec() {
        return CODEC;
    }
}
