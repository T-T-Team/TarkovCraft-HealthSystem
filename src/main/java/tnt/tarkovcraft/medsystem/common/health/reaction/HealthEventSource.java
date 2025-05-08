package tnt.tarkovcraft.medsystem.common.health.reaction;

import tnt.tarkovcraft.core.util.context.Context;

public interface HealthEventSource {

    boolean canReact(Context context);

    HealthEventSourceType<?> getType();
}
