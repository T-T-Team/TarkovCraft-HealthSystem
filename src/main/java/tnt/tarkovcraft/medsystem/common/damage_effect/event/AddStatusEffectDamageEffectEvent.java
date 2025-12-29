package tnt.tarkovcraft.medsystem.common.damage_effect.event;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContextType;
import tnt.tarkovcraft.medsystem.common.damage_effect.function.DamageEffectFunction;
import tnt.tarkovcraft.medsystem.common.damage_effect.function.DamageEffectFunctionType;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectWithDelay;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectEvents;

import java.util.Collections;
import java.util.List;

public record AddStatusEffectDamageEffectEvent(StatusEffectWithDelay effect, List<DamageEffectFunction> durationModifiers, List<DamageEffectFunction> delayModifiers) implements DamageEffectEvent {

    public static final MapCodec<AddStatusEffectDamageEffectEvent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            StatusEffectWithDelay.CODEC.fieldOf("effect").forGetter(AddStatusEffectDamageEffectEvent::effect),
            DamageEffectFunctionType.CODEC.listOf().optionalFieldOf("duration_modifiers", Collections.emptyList()).forGetter(AddStatusEffectDamageEffectEvent::durationModifiers),
            DamageEffectFunctionType.CODEC.listOf().optionalFieldOf("delay_modifiers", Collections.emptyList()).forGetter(AddStatusEffectDamageEffectEvent::delayModifiers)
    ).apply(instance, AddStatusEffectDamageEffectEvent::new));

    @Override
    public void apply(DamageEffectContext context) {
        StatusEffect statusEffect = this.effect.createInstance();
        int duration = DamageEffectFunctionType.applyFunctions(statusEffect.getDuration(), context, this.durationModifiers);
        statusEffect.setDuration(duration);
        StatusEffectHelper.setCausingEntityFromSource(statusEffect, context.damageContext().getSource());
        boolean isGlobalEffect = statusEffect.getType().isGlobalEffect();
        Limb targetLimb = isGlobalEffect ? null : context.limb();
        // at least 1 tick delay is required to prevent CMEs while ticking
        int delay = Math.max(1, DamageEffectFunctionType.applyFunctions(this.effect.delay(), context, this.delayModifiers));
        StatusEffectMap effects = isGlobalEffect ? context.health().getGlobalStatusEffects() : targetLimb.getStatusEffects();
        StatusEffectHelper.addEffect(effects, context.target(), targetLimb, delay, statusEffect);
    }

    @Override
    public void validate(DamageEffectContextType type) {
        this.durationModifiers.forEach(mod -> mod.validate(type));
        this.delayModifiers.forEach(mod -> mod.validate(type));
    }

    @Override
    public DamageEffectEventType<?> getType() {
        return MedSystemDamageEffectEvents.ADD_STATUS_EFFECT.value();
    }
}
