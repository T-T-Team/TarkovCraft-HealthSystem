package tnt.tarkovcraft.medsystem.common.effect.event.function;

import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;

public interface StatusEffectEventFunction {

    int apply(int value, StatusEffectEventContext ctx);

    StatusEffectEventFunctionType<?> getType();
}
