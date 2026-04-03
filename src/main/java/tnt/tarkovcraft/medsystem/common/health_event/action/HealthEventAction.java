package tnt.tarkovcraft.medsystem.common.health_event.action;

import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;

public interface HealthEventAction {

    boolean apply(HealthEventContext ctx);

    HealthEventActionType<?> getType();
}
