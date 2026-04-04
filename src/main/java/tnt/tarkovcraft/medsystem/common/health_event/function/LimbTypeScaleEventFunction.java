package tnt.tarkovcraft.medsystem.common.health_event.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.common.data.number.ConstantNumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventFunctions;

import java.util.Collections;
import java.util.Map;

public record LimbTypeScaleEventFunction(Map<LimbType, NumberProvider> limbTypeScales, NumberProvider scale) implements HealthEventFunction {

    public static final MapCodec<LimbTypeScaleEventFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.unboundedMap(LimbType.CODEC, NumberProviderType.VALUE_CODEC).optionalFieldOf("limb_scaling", Collections.emptyMap()).forGetter(LimbTypeScaleEventFunction::limbTypeScales),
            NumberProviderType.VALUE_CODEC.optionalFieldOf("scale", ConstantNumberProvider.ONE).forGetter(LimbTypeScaleEventFunction::scale)
    ).apply(instance, LimbTypeScaleEventFunction::new));

    @Override
    public float apply(float value, HealthEventContext ctx) {
        Limb limb = ctx.getLimb();
        LimbType limbType = limb.getType();
        NumberProvider provider = this.limbTypeScales.getOrDefault(limbType, this.scale);
        return value * provider.floatValue();
    }

    @Override
    public HealthEventFunctionType<?> getType() {
        return MedSystemHealthEventFunctions.LIMB_TYPE_SCALE.value();
    }
}
