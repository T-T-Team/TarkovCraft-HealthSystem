package tnt.tarkovcraft.medsystem.common.effect;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.StatusEffectEvent;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;

import javax.annotation.Nullable;
import java.util.Collection;

public final class StatusEffectHelper {

    private StatusEffectHelper() {}

    public static void addEffect(StatusEffectMap effects, LivingEntity entity, @Nullable BodyPart bodyPart, StatusEffect effect) {
        addEffect(effects, entity, bodyPart, 0, effect);
    }

    public static void addPostEffect(StatusEffectMap effects, LivingEntity entity, @Nullable BodyPart bodyPart, PostEffect postEffect) {
        addEffect(effects, entity, bodyPart, postEffect.delay(), postEffect.createInstance());
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

    public static Collection<PostEffect> removeEffect(StatusEffectMap effects, LivingEntity entity, @Nullable BodyPart bodyPart, HealthContainer container, Holder<StatusEffectType<?>> holder) {
        return removeEffect(effects, entity, bodyPart, container, holder.value());
    }

    public static Collection<PostEffect> removeEffect(StatusEffectMap effects, LivingEntity entity, @Nullable BodyPart bodyPart, HealthContainer container, StatusEffectType<?> type) {
        return effects.getEffect(type).map(effect -> {
            NeoForge.EVENT_BUS.post(new StatusEffectEvent.Remove(entity, effect, bodyPart));
            return effects.remove(type, container, entity, bodyPart);
        }).orElse(null);
    }
}
