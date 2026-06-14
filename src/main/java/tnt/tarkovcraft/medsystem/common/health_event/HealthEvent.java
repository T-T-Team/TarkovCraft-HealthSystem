package tnt.tarkovcraft.medsystem.common.health_event;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.health_event.action.HealthEventAction;
import tnt.tarkovcraft.medsystem.common.health_event.condition.HealthEventCondition;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Collections;
import java.util.List;

public record HealthEvent(HealthEventTriggerSource eventSource, List<HealthEventCondition> conditions, List<HealthEventAction> actions) {

    public static final Codec<HealthEvent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MedSystemRegistries.HEALTH_EVENT_TRIGGER_SOURCE.byNameCodec().fieldOf("event_source").forGetter(HealthEvent::eventSource),
            Codecs.list(HealthEventCondition.CODEC).optionalFieldOf("conditions", Collections.emptyList()).forGetter(HealthEvent::conditions),
            Codecs.list(HealthEventAction.CODEC).fieldOf("actions").forGetter(HealthEvent::actions)
    ).apply(instance, HealthEvent::new));

    public HealthEventResult trigger(HealthEventContext context) {
        for (HealthEventCondition condition : this.conditions) {
            HealthEventResult conditionResult = condition.test(context);
            if (conditionResult.ordinal() > HealthEventResult.SUCCESS.ordinal()) {
                return conditionResult;
            }
        }

        boolean result = this.actions.stream()
                .allMatch(action -> action.apply(context));
        return result ? HealthEventResult.SUCCESS : HealthEventResult.INVALID;
    }

}
