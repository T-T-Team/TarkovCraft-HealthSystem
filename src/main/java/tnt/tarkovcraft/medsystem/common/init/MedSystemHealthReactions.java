package tnt.tarkovcraft.medsystem.common.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.reaction.*;

import java.util.function.Supplier;

public final class MedSystemHealthReactions {

    public static final DeferredRegister<HealthEventSourceType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.HEALTH_REACTION, MedicalSystem.MOD_ID);

    public static final Supplier<HealthEventSourceType<NoHealthEventSource>> NONE = REGISTRY.register("none", key -> new HealthEventSourceType<>(key, NoHealthEventSource.CODEC));
    public static final Supplier<HealthEventSourceType<ChanceHealthEventSource>> CHANCE = REGISTRY.register("chance", key -> new HealthEventSourceType<>(key, ChanceHealthEventSource.CODEC));
    public static final Supplier<HealthEventSourceType<DamageSourceHealthEventSource>> DAMAGE_SOURCE = REGISTRY.register("damage_source", key -> new HealthEventSourceType<>(key, DamageSourceHealthEventSource.CODEC));
    public static final Supplier<HealthEventSourceType<DeadBodyPartHealthEventSource>> DEAD_BODY_PART = REGISTRY.register("dead_body_part", key -> new HealthEventSourceType<>(key, DeadBodyPartHealthEventSource.CODEC));
    public static final Supplier<HealthEventSourceType<HasStatusEffectHealthEventSource>> HAS_EFFECT = REGISTRY.register("has_effect", key -> new HealthEventSourceType<>(key, HasStatusEffectHealthEventSource.CODEC));

    public static final Supplier<HealthEventSourceType<NotHealthEventSource>> NOT = REGISTRY.register("not", key -> new HealthEventSourceType<>(key, NotHealthEventSource.CODEC));
    public static final Supplier<HealthEventSourceType<OrHealthEventSource>> OR = REGISTRY.register("or", key -> new HealthEventSourceType<>(key, OrHealthEventSource.CODEC));
    public static final Supplier<HealthEventSourceType<AndHealthEventSource>> AND = REGISTRY.register("and", key -> new HealthEventSourceType<>(key, AndHealthEventSource.CODEC));
}
