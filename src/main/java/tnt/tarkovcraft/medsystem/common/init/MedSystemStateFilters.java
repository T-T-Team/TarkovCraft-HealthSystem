package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.state.*;

public final class MedSystemStateFilters {

    public static final DeferredRegister<EntityStateMatcherType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.STATE_MATCHER, MedicalSystem.MOD_ID);

    public static final Holder<EntityStateMatcherType<?>> POSE = REGISTRY.register("pose", key -> new EntityStateMatcherType<>(key, PoseEntityStateMatcher.CODEC));
    public static final Holder<EntityStateMatcherType<?>> UNCONSCIOUS = REGISTRY.register("unconscious", key -> new EntityStateMatcherType<>(key, UnconsciousEntityStateMatcher.CODEC));
    public static final Holder<EntityStateMatcherType<?>> SITTING_PASSENGER = REGISTRY.register("sitting_passenger", key -> new EntityStateMatcherType<>(key, SittingPassengerEntityStateMatcher.CODEC));
    public static final Holder<EntityStateMatcherType<?>> IS_BABY = REGISTRY.register("is_baby", key -> new EntityStateMatcherType<>(key, IsBabyEntityStateMatcher.CODEC));
    public static final Holder<EntityStateMatcherType<?>> LOGICAL = REGISTRY.register("logical", key -> new EntityStateMatcherType<>(key, LogicalEntityStateMatcher.CODEC));
}
