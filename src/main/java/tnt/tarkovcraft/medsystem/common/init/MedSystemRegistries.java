package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.reaction.HealthEventSourceType;
import tnt.tarkovcraft.medsystem.common.health.reaction.function.ChanceFunctionType;
import tnt.tarkovcraft.medsystem.common.health.reaction.event.HealthSourceEventType;
import tnt.tarkovcraft.medsystem.common.health.transform.EntityHitboxTransformType;
import tnt.tarkovcraft.medsystem.common.health.transform.TransformConditionType;

public final class MedSystemRegistries {

    public static final Registry<TransformConditionType<?>> TRANSFORM_CONDITION = new RegistryBuilder<>(Keys.TRANSFORM_CONDITION).create();
    public static final Registry<EntityHitboxTransformType<?>> TRANSFORM = new RegistryBuilder<>(Keys.TRANSFORM).create();
    public static final Registry<StatusEffectType<?>> STATUS_EFFECT = new RegistryBuilder<>(Keys.STATUS_EFFECT).create();
    public static final Registry<HealthEventSourceType<?>> HEALTH_REACTION = new RegistryBuilder<>(Keys.HEALTH_REACTION).create();
    public static final Registry<HealthSourceEventType<?>> HEALTH_REACTION_RESPONSE = new RegistryBuilder<>(Keys.HEALTH_REACTION_RESPONSE).create();
    public static final Registry<ChanceFunctionType<?>> CHANCE_FUNCTION = new RegistryBuilder<>(Keys.CHANCE_FUNCTION).create();

    public static final class Keys {

        public static final ResourceKey<Registry<TransformConditionType<?>>> TRANSFORM_CONDITION = ResourceKey.createRegistryKey(MedicalSystem.resource("transform/condition"));
        public static final ResourceKey<Registry<EntityHitboxTransformType<?>>> TRANSFORM = ResourceKey.createRegistryKey(MedicalSystem.resource("transform/transformer"));
        public static final ResourceKey<Registry<StatusEffectType<?>>> STATUS_EFFECT = ResourceKey.createRegistryKey(MedicalSystem.resource("status_effect"));
        public static final ResourceKey<Registry<HealthEventSourceType<?>>> HEALTH_REACTION = ResourceKey.createRegistryKey(MedicalSystem.resource("health/reaction"));
        public static final ResourceKey<Registry<HealthSourceEventType<?>>> HEALTH_REACTION_RESPONSE = ResourceKey.createRegistryKey(MedicalSystem.resource("health/reaction_response"));
        public static final ResourceKey<Registry<ChanceFunctionType<?>>> CHANCE_FUNCTION = ResourceKey.createRegistryKey(MedicalSystem.resource("health/chance_function"));
    }
}
