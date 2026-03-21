package tnt.tarkovcraft.medsystem.common.effect.event.function;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventParams;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventFunctions;

public final class LostLimbsCountScaleEventFunction implements StatusEffectEventFunction {

    public static final LostLimbsCountScaleEventFunction INSTANCE = new LostLimbsCountScaleEventFunction();
    public static final MapCodec<LostLimbsCountScaleEventFunction> CODEC = MapCodec.unit(INSTANCE);

    private LostLimbsCountScaleEventFunction() {}

    @Override
    public float apply(float value, StatusEffectEventContext ctx) {
        return ctx.getParameterOrDefault(StatusEffectEventParams.LIMBS_LOST, 0) * value;
    }

    @Override
    public StatusEffectEventFunctionType<?> getType() {
        return MedSystemStatusEffectEventFunctions.LOST_LIMB_COUNT.value();
    }
}
