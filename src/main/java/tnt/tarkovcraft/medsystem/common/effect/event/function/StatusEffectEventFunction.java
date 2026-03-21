package tnt.tarkovcraft.medsystem.common.effect.event.function;

import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;

public interface StatusEffectEventFunction {

    float apply(float value, StatusEffectEventContext ctx);

    StatusEffectEventFunctionType<?> getType();
}
