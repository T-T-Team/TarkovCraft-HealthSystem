package tnt.tarkovcraft.medsystem.common.effect;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.StatusEffectEvent;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;

import javax.annotation.Nullable;

public final class StatusEffectHelper {

    private StatusEffectHelper() {}

    public static void addEffect(StatusEffectMap effects, LivingEntity entity, @Nullable BodyPart bodyPart, StatusEffect effect) {
        MedSystemConfig config = MedicalSystem.getConfig();
        if (!config.statusEffects.enableStatusEffects)
            return;
        StatusEffectEvent.Add event = NeoForge.EVENT_BUS.post(new StatusEffectEvent.Add(entity, effect, bodyPart));
        if (event.isCancelled())
            return;
        effects.addEffect(effect);
    }

    public static void replaceEffect(StatusEffectMap effects, LivingEntity entity, @Nullable BodyPart bodyPart, StatusEffect newEffect, @Nullable StatusEffect oldEffect) {
        if (oldEffect != null) {
            NeoForge.EVENT_BUS.post(new StatusEffectEvent.Remove(entity, oldEffect, bodyPart));
        }
        MedSystemConfig config = MedicalSystem.getConfig();
        if (!config.statusEffects.enableStatusEffects)
            return;
        StatusEffectEvent.Add event = NeoForge.EVENT_BUS.post(new StatusEffectEvent.Add(entity, newEffect, bodyPart));
        if (event.isCancelled())
            return;
        effects.replace(newEffect);
    }

    public static StatusEffect removeEffect(StatusEffectMap effects, LivingEntity entity, @Nullable BodyPart bodyPart, Context context, StatusEffectType<?> type) {
        return effects.getEffect(type).map(effect -> {
            NeoForge.EVENT_BUS.post(new StatusEffectEvent.Remove(entity, effect, bodyPart));
            return effects.remove(type, context);
        }).orElse(null);
    }
}
