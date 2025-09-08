package tnt.tarkovcraft.medsystem.common.effect;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.StatusEffectEvent;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;

import javax.annotation.Nullable;

public final class StatusEffectHelper {

    private StatusEffectHelper() {}

    public static void addEffect(StatusEffectMap effects, LivingEntity entity, @Nullable BodyPart bodyPart, StatusEffect effect) {
        addEffect(effects, entity, bodyPart, 0, effect);
    }

    public static void addEffect(StatusEffectMap effects, LivingEntity entity, @Nullable BodyPart bodyPart, int delay, StatusEffect effect) {
        MedSystemConfig config = MedicalSystem.getConfig();
        if (!config.statusEffects.enableStatusEffects)
            return;
        if (delay > 0) {
            StatusEffectEvent.Schedule event = NeoForge.EVENT_BUS.post(new StatusEffectEvent.Schedule(entity, effect, bodyPart, delay));
            if (event.isCancelled())
                return;
            HealthContainer container = HealthSystem.getHealthData(entity);
            container.scheduleStatusEffect(entity, event.getDelay(), bodyPart, effect);
            return;
        }
        StatusEffectEvent.Add event = NeoForge.EVENT_BUS.post(new StatusEffectEvent.Add(entity, effect, bodyPart));
        if (event.isCancelled())
            return;
        effects.addEffect(effect);
        HealthContainer container = HealthSystem.getHealthData(entity);
        container.markStatusEffectAdded(entity);
    }

    public static StatusEffect removeEffect(StatusEffectMap effects, LivingEntity entity, @Nullable BodyPart bodyPart, Context context, StatusEffectType<?> type) {
        return effects.getEffect(type).map(effect -> {
            NeoForge.EVENT_BUS.post(new StatusEffectEvent.Remove(entity, effect, bodyPart));
            return effects.remove(type, context);
        }).orElse(null);
    }
}
