package tnt.tarkovcraft.medsystem.common.effect.event.condition;

import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.TriggerResult;

public interface StatusEffectEventCondition {

    TriggerResult test(StatusEffectEventContext ctx);

    StatusEffectEventConditionType<?> getType();
}
