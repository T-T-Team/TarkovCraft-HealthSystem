package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.health_event.function.*;

public final class MedSystemHealthEventFunctions {

    public static final DeferredRegister<HealthEventFunctionType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.HEALTH_EVENT_FUNCTION, MedSystemConstants.MOD_ID);

    public static final Holder<HealthEventFunctionType<?>> DAMAGE_SCALE = REGISTRY.register("damage_scale", key -> new HealthEventFunctionType<>(key, StatusScaleEventFunction.CODEC));
    public static final Holder<HealthEventFunctionType<?>> DEAD_LIMB_SCALE = REGISTRY.register("dead_limb_scale", key -> new HealthEventFunctionType<>(key, DeadLimbScaleEventFunction.CODEC));
    public static final Holder<HealthEventFunctionType<?>> CALCULATION = REGISTRY.register("calculation", key -> new HealthEventFunctionType<>(key, CalculateEventFunction.CODEC));
    public static final Holder<HealthEventFunctionType<?>> LOST_LIMB_COUNT = REGISTRY.register("lost_limb_count", key -> new HealthEventFunctionType<>(key, LostLimbsCountScaleEventFunction.CODEC));
    public static final Holder<HealthEventFunctionType<?>> LIMB_TYPE_SCALE = REGISTRY.register("limb_type_scale", key -> new HealthEventFunctionType<>(key, LimbTypeScaleEventFunction.CODEC));
    public static final Holder<HealthEventFunctionType<?>> TOOL_MULTIPLIER = REGISTRY.register("tool_multiplier", key -> new HealthEventFunctionType<>(key, ToolMultiplierEventFunction.CODEC));
    public static final Holder<HealthEventFunctionType<?>> SET_VALUE = REGISTRY.register("set_value", key -> new HealthEventFunctionType<>(key, SetValueEventFunction.CODEC));
}
