package tnt.tarkovcraft.medsystem.common.effect.util;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.StatusEffectEvent;
import tnt.tarkovcraft.medsystem.common.advancements.criterion.ReceiveStatusEffectTrigger;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectContext;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.StatusEffectQueue;
import tnt.tarkovcraft.medsystem.common.init.MedSystemCriterionTriggers;

import java.util.Optional;

public final class StatusEffectHelper {

    public static final Marker MARKER = MarkerManager.getMarker("StatusEffects");

    private StatusEffectHelper() {}

    public static void addImmediateGlobalEffect(StatusEffectMap effects, LivingEntity entity, StatusEffect effect) {
        addImmediateEffect(effects, entity, null, effect);
    }

    public static void addImmediateEffect(StatusEffectMap effects, LivingEntity entity, @Nullable Limb limb, StatusEffect effect) {
        addEffect(effects, entity, limb, 0, effect);
    }

    public static void addGlobalEffect(StatusEffectMap effects, LivingEntity entity, int delay, StatusEffect effect) {
        addEffect(effects, entity, null, delay, effect);
    }

    public static void addEffect(StatusEffectMap effects, LivingEntity entity, @Nullable Limb limb, int delay, StatusEffect effect) {
        MedSystemConfig config = MedicalSystem.getConfig();
        if (!config.statusEffects.enableStatusEffects)
            return;
        if (delay > 0) {
            StatusEffectEvent.Schedule event = NeoForge.EVENT_BUS.post(new StatusEffectEvent.Schedule(entity, effect, limb, delay));
            if (event.isCanceled())
                return;
            HealthContainer container = HealthContainer.getAttached(entity);
            MedicalSystem.LOGGER.debug(MARKER, "Scheduling effect {} with delay of {} ticks to target limb \"{}\" with duration {} for entity {}", effect.getType(), event.getDelay(), limb, effect.getDuration(), entity);
            StatusEffectQueue queue = container.getEffectQueue();
            queue.submit(entity.level(), event.getDelay(), limb, effect);
            return;
        }
        StatusEffectEvent.Add event = NeoForge.EVENT_BUS.post(new StatusEffectEvent.Add(entity, effect, limb));
        if (event.isCanceled())
            return;
        MedicalSystem.LOGGER.debug(MARKER, "Adding status effect {} to target limb \"{}\" with duration {} for entity {}", effect.getType(), limb, effect.getDuration(), entity);
        effects.addEffect(effect);
        HealthContainer container = HealthContainer.getAttached(entity);
        StatusEffectMap globalEffects = container.getGlobalStatusEffects();
        if (entity instanceof ServerPlayer player) {
            ReceiveStatusEffectTrigger.triggerCriterion(player, effect);
        }
        globalEffects.painEffectTick(entity, 5, true);
    }

    public static void removeEffect(StatusEffectSubmitter submitter, StatusEffectMap effects, LivingEntity entity, @Nullable Limb limb, HealthContainer container, Holder<StatusEffectType<?>> holder) {
        removeEffect(submitter, effects, entity, limb, container, holder.value());
    }

    public static void removeEffect(StatusEffectSubmitter submitter, StatusEffectMap effects, LivingEntity entity, @Nullable Limb limb, HealthContainer container, StatusEffectType<?> type) {
        StatusEffectContext ctx = StatusEffectContext.of(container, entity, submitter, limb);
        effects.getEffect(type).ifPresent(effect -> {
            NeoForge.EVENT_BUS.post(new StatusEffectEvent.Remove(entity, effect, limb));
            MedicalSystem.LOGGER.debug(MARKER, "Removing status effect {} from target limb {} from entity {}", type, limb, entity);
            effects.remove(type, ctx);
        });
    }

    public static void setCausingEntityFromSource(StatusEffect target, DamageSource source) {
        Entity cause = source.isDirect() ? source.getDirectEntity() : source.getEntity();
        if (cause != null) {
            target.setCausingEntity(cause.getUUID());
        }
    }

    public static boolean hasTaggedEffect(HealthContainer container, TagKey<StatusEffectType<?>> tag) {
        return container.getLimbContainer().getStatusEffects()
                .anyMatch(effect -> effect.getType().is(tag));
    }

    public static Optional<StatusEffect> getAnyTaggedEffect(HealthContainer container, TagKey<StatusEffectType<?>> tag) {
        return container.getLimbContainer().getStatusEffects()
                .filter(effect -> effect.getType().is(tag))
                .findAny();
    }
}
