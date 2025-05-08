package tnt.tarkovcraft.medsystem.common.health.reaction.event;

import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.health.reaction.HealthEventSource;

public interface HealthSourceEvent {

    void onReactionPassed(HealthEventSource source, Context context);

    HealthSourceEventType<?> getType();
}
