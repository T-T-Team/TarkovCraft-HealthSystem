package tnt.tarkovcraft.medsystem.common.health_event.action;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventActions;

public class NoEventAction implements HealthEventAction {

    public static final NoEventAction INSTANCE = new NoEventAction();
    public static final MapCodec<NoEventAction> CODEC = MapCodec.unit(INSTANCE);

    private NoEventAction() {
    }

    @Override
    public boolean apply(HealthEventContext ctx) {
        return false;
    }

    @Override
    public HealthEventActionType<?> getType() {
        return MedSystemHealthEventActions.NONE.value();
    }
}
