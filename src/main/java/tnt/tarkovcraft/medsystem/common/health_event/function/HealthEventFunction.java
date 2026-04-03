package tnt.tarkovcraft.medsystem.common.health_event.function;

import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;

public interface HealthEventFunction {

    float apply(float value, HealthEventContext ctx);

    HealthEventFunctionType<?> getType();
}
