package tnt.tarkovcraft.medsystem.common.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.reaction.event.HealthSourceEventType;
import tnt.tarkovcraft.medsystem.common.health.reaction.event.MobEffectSourceEvent;
import tnt.tarkovcraft.medsystem.common.health.reaction.event.StatusEffectSourceEvent;

import java.util.function.Supplier;

public final class MedSystemHealthReactionResponses {

    public static final DeferredRegister<HealthSourceEventType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.HEALTH_REACTION_RESPONSE, MedicalSystem.MOD_ID);

    public static final Supplier<HealthSourceEventType<StatusEffectSourceEvent>> EFFECT = REGISTRY.register("effect", key -> new HealthSourceEventType<>(key, StatusEffectSourceEvent.CODEC));
    public static final Supplier<HealthSourceEventType<MobEffectSourceEvent>> MOB_EFFECT = REGISTRY.register("mob_effect", key -> new HealthSourceEventType<>(key, MobEffectSourceEvent.CODEC));
}
