package tnt.tarkovcraft.medsystem.common.effect.event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.effect.event.action.StatusEffectEventActionType;
import tnt.tarkovcraft.medsystem.common.effect.event.action.StatusEffectEventAction;
import tnt.tarkovcraft.medsystem.common.effect.event.condition.StatusEffectEventCondition;
import tnt.tarkovcraft.medsystem.common.effect.event.condition.StatusEffectEventConditionType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Collections;
import java.util.List;

public record StatusEffectEvent(StatusEffectEventSource eventSource, List<StatusEffectEventCondition> conditions, List<StatusEffectEventAction> actions) {

    public static final Codec<StatusEffectEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MedSystemRegistries.STATUS_EFFECT_EVENT_SOURCE.byNameCodec().fieldOf("event_source").forGetter(StatusEffectEvent::eventSource),
            Codecs.list(StatusEffectEventConditionType.CODEC).optionalFieldOf("conditions", Collections.emptyList()).forGetter(StatusEffectEvent::conditions),
            Codecs.list(StatusEffectEventActionType.CODEC).fieldOf("actions").forGetter(StatusEffectEvent::actions)
    ).apply(instance, StatusEffectEvent::new));

    public TriggerResult trigger(StatusEffectEventContext context) {
        for (StatusEffectEventCondition condition : this.conditions) {
            TriggerResult conditionResult = condition.test(context);
            if (conditionResult.ordinal() > TriggerResult.SUCCESS.ordinal()) {
                return conditionResult;
            }
        }

        boolean result = this.actions.stream()
                .allMatch(action -> action.apply(context));
        return result ? TriggerResult.SUCCESS : TriggerResult.INVALID;
    }

}
