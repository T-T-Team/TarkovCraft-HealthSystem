package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.heal.predicate.StatusEffectPredicateType;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventSource;
import tnt.tarkovcraft.medsystem.common.effect.event.action.StatusEffectEventActionType;
import tnt.tarkovcraft.medsystem.common.effect.event.condition.StatusEffectEventConditionType;
import tnt.tarkovcraft.medsystem.common.effect.event.function.StatusEffectEventFunctionType;
import tnt.tarkovcraft.medsystem.common.effect.group.EffectGroupItemType;
import tnt.tarkovcraft.medsystem.common.health.state.EntityStateMatcherType;

public final class MedSystemRegistries {

    public static final Registry<StatusEffectType<?>> STATUS_EFFECT = new RegistryBuilder<>(Keys.STATUS_EFFECT).withIntrusiveHolders().create();
    public static final Registry<EffectGroupItemType<?>> EFFECT_GROUP_ITEM = new RegistryBuilder<>(Keys.EFFECT_GROUP_ITEM).create();
    public static final Registry<EntityStateMatcherType<?>> STATE_MATCHER = new RegistryBuilder<>(Keys.STATE_MATCHER).create();
    public static final Registry<StatusEffectPredicateType<?>> STATUS_EFFECT_PREDICATE = new RegistryBuilder<>(Keys.STATUS_EFFECT_PREDICATE).create();

    public static final Registry<StatusEffectEventSource> STATUS_EFFECT_EVENT_SOURCE = new RegistryBuilder<>(Keys.STATUS_EFFECT_EVENT_SOURCE).create();
    public static final Registry<StatusEffectEventConditionType<?>> STATUS_EFFECT_EVENT_CONDITION = new RegistryBuilder<>(Keys.STATUS_EFFECT_EVENT_CONDITION).create();
    public static final Registry<StatusEffectEventActionType<?>> STATUS_EFFECT_EVENT_ACTION = new RegistryBuilder<>(Keys.STATUS_EFFECT_EVENT_ACTION).create();
    public static final Registry<StatusEffectEventFunctionType<?>> STATUS_EFFECT_EVENT_FUNCTION = new RegistryBuilder<>(Keys.STATUS_EFFECT_EVENT_FUNCTION).create();

    public static final class Keys {

        public static final ResourceKey<Registry<StatusEffectType<?>>> STATUS_EFFECT = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("status_effect"));
        public static final ResourceKey<Registry<EffectGroupItemType<?>>> EFFECT_GROUP_ITEM = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("status_effect/effect_group"));
        public static final ResourceKey<Registry<EntityStateMatcherType<?>>> STATE_MATCHER = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("health/state_matcher"));
        public static final ResourceKey<Registry<StatusEffectPredicateType<?>>> STATUS_EFFECT_PREDICATE = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("status_effect/predicate"));
        public static final ResourceKey<Registry<StatusEffectEventSource>> STATUS_EFFECT_EVENT_SOURCE = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("status_effect_event/source"));
        public static final ResourceKey<Registry<StatusEffectEventConditionType<?>>> STATUS_EFFECT_EVENT_CONDITION = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("status_effect_event/condition"));
        public static final ResourceKey<Registry<StatusEffectEventActionType<?>>> STATUS_EFFECT_EVENT_ACTION = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("status_effect_event/action"));
        public static final ResourceKey<Registry<StatusEffectEventFunctionType<?>>> STATUS_EFFECT_EVENT_FUNCTION = ResourceKey.createRegistryKey(MedicalSystem.createIdentifier("status_effect_event/function"));
    }
}
