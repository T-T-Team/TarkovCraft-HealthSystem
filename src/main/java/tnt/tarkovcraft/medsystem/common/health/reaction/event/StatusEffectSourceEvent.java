package tnt.tarkovcraft.medsystem.common.health.reaction.event;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.reaction.HealthEventSource;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactionResponses;

import javax.annotation.Nullable;

public class StatusEffectSourceEvent implements HealthSourceEvent {

    public static final MapCodec<StatusEffectSourceEvent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StatusEffectType.CODEC.fieldOf("effect").forGetter(t -> t.template)
    ).apply(instance, StatusEffectSourceEvent::new));

    private final StatusEffect template;

    public StatusEffectSourceEvent(StatusEffect template) {
        this.template = template;
    }

    @Override
    public void onReactionPassed(HealthEventSource source, HealthContainer container, LivingEntity entity, @Nullable DamageSource damageSource, BodyPart limb) {
        StatusEffectType<?> type = this.template.getType();
        StatusEffectMap map = type.isGlobalEffect() ? container.getGlobalStatusEffects() : limb.getStatusEffects();
        StatusEffectHelper.addEffect(map, entity, limb, this.template.copy());
    }

    @Override
    public HealthSourceEventType<?> getType() {
        return MedSystemHealthReactionResponses.EFFECT.get();
    }
}
