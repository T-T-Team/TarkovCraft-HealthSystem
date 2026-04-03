package tnt.tarkovcraft.medsystem.common.health_event.function;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventParams;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventFunctions;

public final class LostLimbsCountScaleEventFunction implements HealthEventFunction {

    public static final LostLimbsCountScaleEventFunction INSTANCE = new LostLimbsCountScaleEventFunction();
    public static final MapCodec<LostLimbsCountScaleEventFunction> CODEC = MapCodec.unit(INSTANCE);

    private LostLimbsCountScaleEventFunction() {}

    @Override
    public float apply(float value, HealthEventContext ctx) {
        return ctx.getParameterOrDefault(HealthEventParams.LIMBS_LOST, 0) * value;
    }

    @Override
    public HealthEventFunctionType<?> getType() {
        return MedSystemHealthEventFunctions.LOST_LIMB_COUNT.value();
    }
}
