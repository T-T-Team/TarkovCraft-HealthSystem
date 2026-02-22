package tnt.tarkovcraft.medsystem.common.effect.event.action;

import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;

public interface StatusEffectEventAction {

    boolean apply(StatusEffectEventContext ctx);

    StatusEffectEventActionType<?> getType();
}
