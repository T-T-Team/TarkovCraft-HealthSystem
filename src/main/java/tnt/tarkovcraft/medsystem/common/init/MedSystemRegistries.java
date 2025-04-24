package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.transform.EntityHitboxTransformType;
import tnt.tarkovcraft.medsystem.common.health.transform.TransformConditionType;

public final class MedSystemRegistries {

    public static final Registry<TransformConditionType<?>> TRANSFORM_CONDITION = new RegistryBuilder<>(Keys.TRANSFORM_CONDITION).create();
    public static final Registry<EntityHitboxTransformType<?>> TRANSFORM = new RegistryBuilder<>(Keys.TRANSFORM).create();

    public static final class Keys {

        public static final ResourceKey<Registry<TransformConditionType<?>>> TRANSFORM_CONDITION = ResourceKey.createRegistryKey(MedicalSystem.resource("transform/condition"));
        public static final ResourceKey<Registry<EntityHitboxTransformType<?>>> TRANSFORM = ResourceKey.createRegistryKey(MedicalSystem.resource("transform/transformer"));
    }
}
