package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.effect.event.condition.*;

public final class MedSystemStatusEffectEventConditions {

    public static final DeferredRegister<StatusEffectEventConditionType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.STATUS_EFFECT_EVENT_CONDITION, MedSystemConstants.MOD_ID);

    public static final Holder<StatusEffectEventConditionType<?>> DAMAGE_RANGE = REGISTRY.register("damage_range", key -> new StatusEffectEventConditionType<>(key, StatusRangeEventCondition.CODEC));
    public static final Holder<StatusEffectEventConditionType<?>> IS_LIMB = REGISTRY.register("is_limb", key -> new StatusEffectEventConditionType<>(key, IsLimbTypeStatusEffectEventCondition.CODEC));
    public static final Holder<StatusEffectEventConditionType<?>> IS_DAMAGE = REGISTRY.register("is_damage", key -> new StatusEffectEventConditionType<>(key, IsDamageTypeStatusEffectEventCondition.CODEC));
    public static final Holder<StatusEffectEventConditionType<?>> IS_DEAD_LIMB = REGISTRY.register("is_dead_limb", key -> new StatusEffectEventConditionType<>(key, IsDeadLimbStatusEffectEventCondition.CODEC));
    public static final Holder<StatusEffectEventConditionType<?>> IS_ROOT_LIMB = REGISTRY.register("is_root_limb", key -> new StatusEffectEventConditionType<>(key, IsRootLimbStatusEffectEventCondition.CODEC));
    public static final Holder<StatusEffectEventConditionType<?>> HAS_DEAD_LIMB = REGISTRY.register("has_dead_limb", key -> new StatusEffectEventConditionType<>(key, HasDeadLimbStatusEffectEventCondition.CODEC));
    public static final Holder<StatusEffectEventConditionType<?>> HAS_EFFECT = REGISTRY.register("has_effect", key -> new StatusEffectEventConditionType<>(key, HasStatusEffectStatusEventCondition.CODEC));
    public static final Holder<StatusEffectEventConditionType<?>> RANDOM_CHANCE = REGISTRY.register("random_chance", key -> new StatusEffectEventConditionType<>(key, RandomChanceStatusEffectEventCondition.CODEC));
    public static final Holder<StatusEffectEventConditionType<?>> FALL_FRACTURE = REGISTRY.register("fall_fracture", key -> new StatusEffectEventConditionType<>(key, FallStatusEffectEventCondition.CODEC));
    public static final Holder<StatusEffectEventConditionType<?>> LOGICAL = REGISTRY.register("logical", key -> new StatusEffectEventConditionType<>(key, LogicalFunctionStatusEffectEventCondition.CODEC));
    public static final Holder<StatusEffectEventConditionType<?>> LOST_LIMB = REGISTRY.register("lost_limb", key -> new StatusEffectEventConditionType<>(key, LostLimbStatusEffectCondition.CODEC));
}
