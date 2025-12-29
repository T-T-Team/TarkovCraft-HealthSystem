package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.common.damage_effect.condition.*;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;

public final class MedSystemDamageEffectConditions {

    public static final DeferredRegister<DamageEffectConditionType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.DAMAGE_EFFECT_CONDITION, MedSystemConstants.MOD_ID);

    public static final Holder<DamageEffectConditionType<?>> DAMAGE_RANGE = REGISTRY.register("damage_range", key -> new DamageEffectConditionType<>(key, DamageRangeCondition.CODEC));
    public static final Holder<DamageEffectConditionType<?>> IS_LIMB = REGISTRY.register("is_limb", key -> new DamageEffectConditionType<>(key, IsLimbTypeDamageEffectCondition.CODEC));
    public static final Holder<DamageEffectConditionType<?>> IS_DAMAGE = REGISTRY.register("is_damage", key -> new DamageEffectConditionType<>(key, IsDamageTypeDamageEffectCondition.CODEC));
    public static final Holder<DamageEffectConditionType<?>> IS_DEAD_LIMB = REGISTRY.register("is_dead_limb", key -> new DamageEffectConditionType<>(key, IsDeadLimbDamageEffectCondition.CODEC));
    public static final Holder<DamageEffectConditionType<?>> HAS_EFFECT = REGISTRY.register("has_effect", key -> new DamageEffectConditionType<>(key, HasStatusEffectDamageCondition.CODEC));
    public static final Holder<DamageEffectConditionType<?>> RANDOM_CHANCE = REGISTRY.register("random_chance", key -> new DamageEffectConditionType<>(key, RandomChanceDamageEffectCondition.CODEC));
    public static final Holder<DamageEffectConditionType<?>> FALL_FRACTURE = REGISTRY.register("fall_fracture", key -> new DamageEffectConditionType<>(key, FallDamageEffectCondition.CODEC));
    public static final Holder<DamageEffectConditionType<?>> LOGICAL = REGISTRY.register("logical", key -> new DamageEffectConditionType<>(key, LogicalFunctionDamageEffectCondition.CODEC));
}
