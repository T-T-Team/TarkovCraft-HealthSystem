package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.damage_effect.condition.DamageEffectConditionType;
import tnt.tarkovcraft.medsystem.common.damage_effect.event.DamageEffectEventType;
import tnt.tarkovcraft.medsystem.common.damage_effect.function.DamageEffectFunctionType;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.group.EffectGroupItemType;
import tnt.tarkovcraft.medsystem.common.health.state.StateFilterType;
import tnt.tarkovcraft.medsystem.common.health.transform.EntityHitboxTransformType;
import tnt.tarkovcraft.medsystem.common.health.transform.TransformConditionType;

public final class MedSystemRegistries {

    @Deprecated
    public static final Registry<TransformConditionType<?>> TRANSFORM_CONDITION = new RegistryBuilder<>(Keys.TRANSFORM_CONDITION).create();
    @Deprecated
    public static final Registry<EntityHitboxTransformType<?>> TRANSFORM = new RegistryBuilder<>(Keys.TRANSFORM).create();
    public static final Registry<StatusEffectType<?>> STATUS_EFFECT = new RegistryBuilder<>(Keys.STATUS_EFFECT).withIntrusiveHolders().create();
    public static final Registry<EffectGroupItemType<?>> EFFECT_GROUP_ITEM = new RegistryBuilder<>(Keys.EFFECT_GROUP_ITEM).create();
    public static final Registry<DamageEffectConditionType<?>> DAMAGE_EFFECT_CONDITION = new RegistryBuilder<>(Keys.DAMAGE_EFFECT_CONDITION).create();
    public static final Registry<DamageEffectFunctionType<?>> DAMAGE_EFFECT_FUNCTION = new RegistryBuilder<>(Keys.DAMAGE_EFFECT_FUNCTION).create();
    public static final Registry<DamageEffectEventType<?>> DAMAGE_EFFECT_EVENT = new RegistryBuilder<>(Keys.DAMAGE_EFFECT_EVENT).create();
    public static final Registry<StateFilterType<?>> STATE_FILTER = new RegistryBuilder<>(Keys.STATE_FILTER).create();

    public static final class Keys {

        public static final ResourceKey<Registry<TransformConditionType<?>>> TRANSFORM_CONDITION = ResourceKey.createRegistryKey(MedicalSystem.resource("transform/condition"));
        public static final ResourceKey<Registry<EntityHitboxTransformType<?>>> TRANSFORM = ResourceKey.createRegistryKey(MedicalSystem.resource("transform/transformer"));
        public static final ResourceKey<Registry<StatusEffectType<?>>> STATUS_EFFECT = ResourceKey.createRegistryKey(MedicalSystem.resource("status_effect"));
        public static final ResourceKey<Registry<EffectGroupItemType<?>>> EFFECT_GROUP_ITEM = ResourceKey.createRegistryKey(MedicalSystem.resource("status_effect_group_item"));
        public static final ResourceKey<Registry<DamageEffectConditionType<?>>> DAMAGE_EFFECT_CONDITION = ResourceKey.createRegistryKey(MedicalSystem.resource("damage_effect/condition"));
        public static final ResourceKey<Registry<DamageEffectFunctionType<?>>> DAMAGE_EFFECT_FUNCTION = ResourceKey.createRegistryKey(MedicalSystem.resource("damage_effect/function"));
        public static final ResourceKey<Registry<DamageEffectEventType<?>>> DAMAGE_EFFECT_EVENT = ResourceKey.createRegistryKey(MedicalSystem.resource("damage_effect/event"));
        public static final ResourceKey<Registry<StateFilterType<?>>> STATE_FILTER = ResourceKey.createRegistryKey(MedicalSystem.resource("health/state_filter"));
    }
}
