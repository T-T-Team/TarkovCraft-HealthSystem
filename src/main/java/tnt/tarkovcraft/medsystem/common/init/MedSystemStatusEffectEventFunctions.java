package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.effect.event.function.*;

public final class MedSystemStatusEffectEventFunctions {

    public static final DeferredRegister<StatusEffectEventFunctionType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.STATUS_EFFECT_EVENT_FUNCTION, MedSystemConstants.MOD_ID);

    public static final Holder<StatusEffectEventFunctionType<?>> DAMAGE_SCALE = REGISTRY.register("damage_scale", key -> new StatusEffectEventFunctionType<>(key, StatusScaleEventFunction.CODEC));
    public static final Holder<StatusEffectEventFunctionType<?>> DEAD_LIMB_SCALE = REGISTRY.register("dead_limb_scale", key -> new StatusEffectEventFunctionType<>(key, DeadLimbScaleEventFunction.CODEC));
    public static final Holder<StatusEffectEventFunctionType<?>> CALCULATION = REGISTRY.register("calculation", key -> new StatusEffectEventFunctionType<>(key, CalculateEventFunction.CODEC));
    public static final Holder<StatusEffectEventFunctionType<?>> LOST_LIMB_COUNT = REGISTRY.register("lost_limb_count", key -> new StatusEffectEventFunctionType<>(key, LostLimbsCountScaleEventFunction.CODEC));
    public static final Holder<StatusEffectEventFunctionType<?>> LIMB_TYPE_SCALE = REGISTRY.register("limb_type_scale", key -> new StatusEffectEventFunctionType<>(key, LimbTypeScaleEventFunction.CODEC));
}
