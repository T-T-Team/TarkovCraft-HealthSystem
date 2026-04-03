package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.health_event.condition.*;

public final class MedSystemHealthEventConditions {

    public static final DeferredRegister<HealthEventConditionType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.HEALTH_EVENT_CONDITION, MedSystemConstants.MOD_ID);

    public static final Holder<HealthEventConditionType<?>> DAMAGE_RANGE = REGISTRY.register("damage_range", key -> new HealthEventConditionType<>(key, StatusRangeEventCondition.CODEC));
    public static final Holder<HealthEventConditionType<?>> IS_LIMB = REGISTRY.register("is_limb", key -> new HealthEventConditionType<>(key, IsLimbTypeEventCondition.CODEC));
    public static final Holder<HealthEventConditionType<?>> IS_DAMAGE = REGISTRY.register("is_damage", key -> new HealthEventConditionType<>(key, IsDamageTypeEventCondition.CODEC));
    public static final Holder<HealthEventConditionType<?>> IS_DEAD_LIMB = REGISTRY.register("is_dead_limb", key -> new HealthEventConditionType<>(key, IsDeadLimbEventCondition.CODEC));
    public static final Holder<HealthEventConditionType<?>> IS_ROOT_LIMB = REGISTRY.register("is_root_limb", key -> new HealthEventConditionType<>(key, IsRootLimbEventCondition.CODEC));
    public static final Holder<HealthEventConditionType<?>> HAS_DEAD_LIMB = REGISTRY.register("has_dead_limb", key -> new HealthEventConditionType<>(key, HasDeadLimbEventCondition.CODEC));
    public static final Holder<HealthEventConditionType<?>> HAS_EFFECT = REGISTRY.register("has_effect", key -> new HealthEventConditionType<>(key, HasStatusEffectEventCondition.CODEC));
    public static final Holder<HealthEventConditionType<?>> RANDOM_CHANCE = REGISTRY.register("random_chance", key -> new HealthEventConditionType<>(key, RandomChanceEventCondition.CODEC));
    public static final Holder<HealthEventConditionType<?>> FALL_FRACTURE = REGISTRY.register("fall_fracture", key -> new HealthEventConditionType<>(key, FallEventCondition.CODEC));
    public static final Holder<HealthEventConditionType<?>> LOGICAL = REGISTRY.register("logical", key -> new HealthEventConditionType<>(key, LogicalEventCondition.CODEC));
    public static final Holder<HealthEventConditionType<?>> LOST_LIMB = REGISTRY.register("lost_limb", key -> new HealthEventConditionType<>(key, LostLimbEventCondition.CODEC));
    public static final Holder<HealthEventConditionType<?>> ITEM_PREDICATE = REGISTRY.register("item_predicate", key -> new HealthEventConditionType<>(key, ItemPredicateEventCondition.CODEC));
}
