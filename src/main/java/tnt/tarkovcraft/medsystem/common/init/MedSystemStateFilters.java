package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.state.*;

public final class MedSystemStateFilters {

    public static final DeferredRegister<StateFilterType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.STATE_FILTER, MedicalSystem.MOD_ID);

    public static final Holder<StateFilterType<?>> POSE = REGISTRY.register("pose", key -> new StateFilterType<>(key, PoseStateFilter.CODEC));
    public static final Holder<StateFilterType<?>> UNCONSCIOUS = REGISTRY.register("unconscious", key -> new StateFilterType<>(key, UnconsciousStateFilter.CODEC));
    public static final Holder<StateFilterType<?>> SITTING_PASSENGER = REGISTRY.register("sitting_passenger", key -> new StateFilterType<>(key, SittingPassengerStateFilter.CODEC));
    public static final Holder<StateFilterType<?>> IS_BABY = REGISTRY.register("is_baby", key -> new StateFilterType<>(key, IsBabyStateFilter.CODEC));
    public static final Holder<StateFilterType<?>> LOGICAL = REGISTRY.register("logical", key -> new StateFilterType<>(key, LogicalStateFilter.CODEC));
}
