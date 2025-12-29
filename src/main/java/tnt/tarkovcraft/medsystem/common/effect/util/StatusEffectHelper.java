package tnt.tarkovcraft.medsystem.common.effect.util;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.StatusEffectEvent;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import javax.annotation.Nullable;

public final class StatusEffectHelper {

    private StatusEffectHelper() {}

    public static void addEffect(StatusEffectMap effects, LivingEntity entity, @Nullable Limb limb, StatusEffect effect) {
        addEffect(effects, entity, limb, 0, effect);
    }

    public static void handleSubmittedEffects(StatusEffectMap effects, LivingEntity entity, @Nullable Limb limb, ListStatusEffectSubmitter submitter) {
        submitter.forEach(post -> addPostEffect(effects, entity, limb, post));
    }

    public static void addPostEffect(StatusEffectMap effects, LivingEntity entity, @Nullable Limb limb, StatusEffectWithDelay statusEffectWithDelay) {
        addEffect(effects, entity, limb, statusEffectWithDelay.delay(), statusEffectWithDelay.createInstance());
    }

    public static void addEffect(StatusEffectMap effects, LivingEntity entity, @Nullable Limb limb, int delay, StatusEffect effect) {
        MedSystemConfig config = MedicalSystem.getConfig();
        if (!config.statusEffects.enableStatusEffects)
            return;
        if (delay > 0) {
            StatusEffectEvent.Schedule event = NeoForge.EVENT_BUS.post(new StatusEffectEvent.Schedule(entity, effect, limb, delay));
            if (event.isCancelled())
                return;
            HealthContainer container = HealthSystem.getHealthData(entity);
            container.scheduleStatusEffect(entity, event.getDelay(), limb, effect);
            return;
        }
        StatusEffectEvent.Add event = NeoForge.EVENT_BUS.post(new StatusEffectEvent.Add(entity, effect, limb));
        if (event.isCancelled())
            return;
        effects.addEffect(effect);
        HealthContainer container = HealthSystem.getHealthData(entity);
        container.markStatusEffectAdded(entity);
    }

    public static void removeEffect(StatusEffectSubmitter submitter, StatusEffectMap effects, LivingEntity entity, @Nullable Limb limb, HealthContainer container, Holder<StatusEffectType<?>> holder) {
        removeEffect(submitter, effects, entity, limb, container, holder.value());
    }

    public static void removeEffect(StatusEffectSubmitter submitter, StatusEffectMap effects, LivingEntity entity, @Nullable Limb limb, HealthContainer container, StatusEffectType<?> type) {
        effects.getEffect(type).ifPresent(effect -> {
            NeoForge.EVENT_BUS.post(new StatusEffectEvent.Remove(entity, effect, limb));
            effects.remove(submitter, type, container, entity, limb);
        });
    }

    public static void setCausingEntityFromSource(StatusEffect target, DamageSource source) {
        Entity cause = source.isDirect() ? source.getDirectEntity() : source.getEntity();
        if (cause != null) {
            target.setCausingEntity(cause.getUUID());
        }
    }
}
