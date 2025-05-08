package tnt.tarkovcraft.medsystem.common.health.reaction.event;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.MedicalSystemContextKeys;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.reaction.HealthEventSource;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactionResponses;

public class StatusEffectSourceEvent implements HealthSourceEvent {

    public static final MapCodec<StatusEffectSourceEvent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StatusEffectType.CODEC.fieldOf("effect").forGetter(t -> t.template)
    ).apply(instance, StatusEffectSourceEvent::new));

    private final StatusEffect template;

    public StatusEffectSourceEvent(StatusEffect template) {
        this.template = template;
    }

    @Override
    public void onReactionPassed(HealthEventSource source, Context context) {
        StatusEffectType<?> type = this.template.getType();
        HealthContainer definition = context.getOrThrow(MedicalSystemContextKeys.HEALTH_CONTAINER);
        BodyPart part = context.getOrThrow(MedicalSystemContextKeys.BODY_PART);
        StatusEffectMap map = type.isGlobalEffect() ? definition.getGlobalStatusEffects() : part.getStatusEffects();
        map.addEffect(this.template.copy());
    }

    @Override
    public HealthSourceEventType<?> getType() {
        return MedSystemHealthReactionResponses.EFFECT.get();
    }
}
