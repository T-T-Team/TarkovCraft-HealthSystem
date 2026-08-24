package tnt.tarkovcraft.medsystem.common.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.api.heal.EffectRecoveryApplicator;
import tnt.tarkovcraft.medsystem.common.blood_system.effect.*;
import tnt.tarkovcraft.medsystem.common.damage.condition.DamageCondition;
import tnt.tarkovcraft.medsystem.common.damage.condition.DamageSourceCondition;
import tnt.tarkovcraft.medsystem.common.damage.condition.DamageTypeCondition;
import tnt.tarkovcraft.medsystem.common.damage.condition.IsSpecificLimbDamage;
import tnt.tarkovcraft.medsystem.common.damage.function.*;
import tnt.tarkovcraft.medsystem.common.consume_effect.ConsumeEffectType;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.group.*;
import tnt.tarkovcraft.medsystem.common.health.applicator.BleedEffectRecoveryApplicator;
import tnt.tarkovcraft.medsystem.common.health.applicator.FractureEffectRecoveryApplicator;
import tnt.tarkovcraft.medsystem.common.health.applicator.SimpleEffectRecoveryApplicator;
import tnt.tarkovcraft.medsystem.common.health.applicator.StagedEffectRecoveryApplicator;
import tnt.tarkovcraft.medsystem.common.health.state.*;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventTriggerSource;
import tnt.tarkovcraft.medsystem.common.health_event.action.*;
import tnt.tarkovcraft.medsystem.common.health_event.condition.*;
import tnt.tarkovcraft.medsystem.common.health_event.function.*;

public final class MedSystemRegistries {

    public static final Registry<StatusEffectType<?>> STATUS_EFFECT = new RegistryBuilder<>(Keys.STATUS_EFFECT).withIntrusiveHolders().create();
    public static final Registry<MapCodec<? extends EffectGroupItem>> EFFECT_GROUP_ITEM = new RegistryBuilder<>(Keys.EFFECT_GROUP_ITEM).create();
    public static final Registry<MapCodec<? extends EntityStateMatcher>> STATE_MATCHER = new RegistryBuilder<>(Keys.STATE_MATCHER).create();

    public static final Registry<MapCodec<? extends EffectRecoveryApplicator>> EFFECT_RECOVERY_APPLICATOR = new RegistryBuilder<>(Keys.EFFECT_RECOVERY_APPLICATOR).create();

    public static final Registry<MapCodec<? extends DamageCondition>> DAMAGE_CONDITIONS = new RegistryBuilder<>(Keys.DAMAGE_CONDITIONS).create();
    public static final Registry<MapCodec<? extends DamageFunction>> DAMAGE_FUNCTIONS = new RegistryBuilder<>(Keys.DAMAGE_FUNCTIONS).create();

    public static final Registry<HealthEventTriggerSource> HEALTH_EVENT_TRIGGER_SOURCE = new RegistryBuilder<>(Keys.HEALTH_EVENT_TRIGGER_SOURCE).create();
    public static final Registry<MapCodec<? extends HealthEventCondition>> HEALTH_EVENT_CONDITION = new RegistryBuilder<>(Keys.HEALTH_EVENT_CONDITION).create();
    public static final Registry<MapCodec<? extends HealthEventAction>> HEALTH_EVENT_ACTION = new RegistryBuilder<>(Keys.HEALTH_EVENT_ACTION).create();
    public static final Registry<MapCodec<? extends HealthEventFunction>> HEALTH_EVENT_FUNCTION = new RegistryBuilder<>(Keys.HEALTH_EVENT_FUNCTION).create();

    public static final Registry<MapCodec<? extends BloodLevelEffect>> BLOOD_LEVEL_EFFECT = new RegistryBuilder<>(Keys.BLOOD_LEVEL_EFFECT).create();
    public static final Registry<ConsumeEffectType<?>> CONSUME_EFFECT = new RegistryBuilder<>(Keys.CONSUME_EFFECT).create();

    public static void registerEffectGroupItems(RegisterEvent.RegisterHelper<MapCodec<? extends EffectGroupItem>> helper) {
        registerObject(helper, "attribute", AttributeModifierEffectGroupItem.CODEC);
        registerObject(helper, "health", HealthEffectGroupItem.CODEC);
        registerObject(helper, "status_effect_removing", StatusEffectRemovingEffectGroupItem.CODEC);
        registerObject(helper, "dead_limb_recovery", DeadLimbRecoveryEffectGroupItem.CODEC);
        registerObject(helper, "blood_recovery", BloodRecoveryEffectGroupItem.CODEC);
        registerObject(helper, "mob_effect", MobEffectGroupItem.CODEC);
    }

    public static void registerStateMatchers(RegisterEvent.RegisterHelper<MapCodec<? extends EntityStateMatcher>> helper) {
        registerObject(helper, "pose", PoseEntityStateMatcher.CODEC);
        registerObject(helper, "custom_pose", CustomEntityPoseStateMatcher.CODEC);
        registerObject(helper, "sitting_passenger", SittingPassengerEntityStateMatcher.CODEC);
        registerObject(helper, "is_baby", IsBabyEntityStateMatcher.CODEC);
        registerObject(helper, "logical", LogicalEntityStateMatcher.CODEC);
    }

    public static void registerEffectRecoveryApplicators(RegisterEvent.RegisterHelper<MapCodec<? extends EffectRecoveryApplicator>> helper) {
        registerObject(helper, "simple", SimpleEffectRecoveryApplicator.CODEC);
        registerObject(helper, "bleed", BleedEffectRecoveryApplicator.CODEC);
        registerObject(helper, "fracture", FractureEffectRecoveryApplicator.CODEC);
        registerObject(helper, "staged", StagedEffectRecoveryApplicator.CODEC);
    }

    public static void registerDamageConditions(RegisterEvent.RegisterHelper<MapCodec<? extends DamageCondition>> helper) {
        registerObject(helper, "builtin/specific_limb", IsSpecificLimbDamage.CODEC);
        registerObject(helper, "damage_predicate", DamageSourceCondition.CODEC);
        registerObject(helper, "damage_type", DamageTypeCondition.CODEC);
    }

    public static void registerDamageFunctions(RegisterEvent.RegisterHelper<MapCodec<? extends DamageFunction>> helper) {
        registerObject(helper, "builtin/generic", GenericDamageFunction.CODEC);
        registerObject(helper, "builtin/specific_limb", SpecificLimbDamageFunction.CODEC);
        registerObject(helper, "builtin/broken_limb", BrokenLimbDamageFunction.CODEC);
        registerObject(helper, "builtin/poison", PoisonDamageFunction.CODEC);
        registerObject(helper, "melee_damage", MeleeHitFunction.CODEC);
        registerObject(helper, "projectile_damage", ProjectileDamageFunction.CODEC);
        registerObject(helper, "fall", FallDamageFunction.CODEC);
        registerObject(helper, "explosion", ExplosionDamageFunction.CODEC);
        registerObject(helper, "in_liquid", InLiquidDamageFunction.CODEC);
    }

    public static void registerHealthEventConditions(RegisterEvent.RegisterHelper<MapCodec<? extends HealthEventCondition>> helper) {
        registerObject(helper, "damage_range", StatusRangeEventCondition.CODEC);
        registerObject(helper, "is_limb", IsLimbTypeEventCondition.CODEC);
        registerObject(helper, "is_damage", IsDamageTypeEventCondition.CODEC);
        registerObject(helper, "is_dead_limb", IsDeadLimbEventCondition.CODEC);
        registerObject(helper, "is_root_limb", IsRootLimbEventCondition.CODEC);
        registerObject(helper, "has_dead_limb", HasDeadLimbEventCondition.CODEC);
        registerObject(helper, "has_effect", HasStatusEffectEventCondition.CODEC);
        registerObject(helper, "random_chance", RandomChanceEventCondition.CODEC);
        registerObject(helper, "fall_fracture", FallEventCondition.CODEC);
        registerObject(helper, "logical", LogicalEventCondition.CODEC);
        registerObject(helper, "lost_limb", LostLimbEventCondition.CODEC);
        registerObject(helper, "item_predicate", ItemPredicateEventCondition.CODEC);
        registerObject(helper, "has_limb_tag", IsTaggedLimbEventCondition.CODEC);
    }

    public static void registerHealthEventActions(RegisterEvent.RegisterHelper<MapCodec<? extends HealthEventAction>> helper) {
        registerObject(helper, "none", NoEventAction.CODEC);
        registerObject(helper, "add_status_effect", AddStatusEffectEventAction.CODEC);
        registerObject(helper, "add_mob_effect", AddMobEffectEventAction.CODEC);
        registerObject(helper, "add_shock", AddShockEventAction.CODEC);
        registerObject(helper, "add_bleed", AddBleedEventAction.CODEC);
        registerObject(helper, "copy_incoming_effects", CopyIncomingEffectsEventAction.CODEC);
        registerObject(helper, "weighted", WeightedEventAction.CODEC);
    }

    public static void registerHealthEventFunctions(RegisterEvent.RegisterHelper<MapCodec<? extends HealthEventFunction>> helper) {
        registerObject(helper, "damage_scale", StatusScaleEventFunction.CODEC);
        registerObject(helper, "dead_limb_scale", DeadLimbScaleEventFunction.CODEC);
        registerObject(helper, "calculation", CalculateEventFunction.CODEC);
        registerObject(helper, "lost_limb_count", LostLimbsCountScaleEventFunction.CODEC);
        registerObject(helper, "limb_type_scale", LimbTypeScaleEventFunction.CODEC);
        registerObject(helper, "tool_multiplier", ToolMultiplierEventFunction.CODEC);
        registerObject(helper, "set_value", SetValueEventFunction.CODEC);
    }

    public static void registerBloodLevelEffects(RegisterEvent.RegisterHelper<MapCodec<? extends BloodLevelEffect>> helper) {
        registerObject(helper, "death", DeathBloodLevelEffect.CODEC);
        registerObject(helper, "unconscious", UnconsciousBloodLevelEffect.CODEC);
        registerObject(helper, "configurable_unconscious", ConfigurableUnconsciousBloodLevelEffect.CODEC);
        registerObject(helper, "apply_unconscious_config", ApplyUnconsciousConfigBloodLevelEffect.CODEC);
        registerObject(helper, "add_vanilla_attribute_modifier", AddVanillaAttributeModifierBloodLevelEffect.CODEC);
        registerObject(helper, "add_attribute_modifier", AddAttributeModifierBloodLevelEffect.CODEC);
        registerObject(helper, "remove_vanilla_attribute_modifier", RemoveVanillaAttributeModifierBloodLevelEffect.CODEC);
        registerObject(helper, "remove_attribute_modifier", RemoveAttributeModifierBloodLevelEffect.CODEC);
    }

    private static <T> void registerObject(RegisterEvent.RegisterHelper<T> helper, String name, T object) {
        helper.register(ResourceLocation.fromNamespaceAndPath(MedSystemConstants.MOD_ID, name), object);
    }

    public static final class Keys {

        public static final ResourceKey<Registry<StatusEffectType<?>>> STATUS_EFFECT = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("status_effect"));
        public static final ResourceKey<Registry<MapCodec<? extends EffectGroupItem>>> EFFECT_GROUP_ITEM = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("status_effect/effect_group"));

        public static final ResourceKey<Registry<MapCodec<? extends EntityStateMatcher>>> STATE_MATCHER = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("health/state_matcher"));

        public static final ResourceKey<Registry<MapCodec<? extends EffectRecoveryApplicator>>> EFFECT_RECOVERY_APPLICATOR = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("effect_recovery_applicator"));

        public static final ResourceKey<Registry<MapCodec<? extends DamageCondition>>> DAMAGE_CONDITIONS = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("damage_resolver/condition"));
        public static final ResourceKey<Registry<MapCodec<? extends DamageFunction>>> DAMAGE_FUNCTIONS = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("damage_resolver/function"));

        public static final ResourceKey<Registry<HealthEventTriggerSource>> HEALTH_EVENT_TRIGGER_SOURCE = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("health_event/source"));
        public static final ResourceKey<Registry<MapCodec<? extends HealthEventCondition>>> HEALTH_EVENT_CONDITION = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("health_event/condition"));
        public static final ResourceKey<Registry<MapCodec<? extends HealthEventAction>>> HEALTH_EVENT_ACTION = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("health_event/action"));
        public static final ResourceKey<Registry<MapCodec<? extends HealthEventFunction>>> HEALTH_EVENT_FUNCTION = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("health_event/function"));

        public static final ResourceKey<Registry<MapCodec<? extends BloodLevelEffect>>> BLOOD_LEVEL_EFFECT = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("blood_system/effect"));
        public static final ResourceKey<Registry<ConsumeEffectType<?>>> CONSUME_EFFECT = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("consume_effect"));
    }
}
