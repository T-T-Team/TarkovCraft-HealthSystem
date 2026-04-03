package tnt.tarkovcraft.medsystem.common.health_event.condition;

import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;

public interface HealthEventCondition {

    HealthEventResult test(HealthEventContext ctx);

    HealthEventConditionType<?> getType();
}
