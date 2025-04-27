package tnt.tarkovcraft.medsystem.common.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.transform.*;

import java.util.function.Supplier;

public final class MedSystemTransformConditions {

    public static final DeferredRegister<TransformConditionType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.TRANSFORM_CONDITION, MedicalSystem.MOD_ID);

    public static final Supplier<TransformConditionType<NotTransformCondition>> NOT = REGISTRY.register("not", key -> new TransformConditionType<>(key, NotTransformCondition.CODEC));
    public static final Supplier<TransformConditionType<EntityPoseTransformCondition>> ENTITY_POSE = REGISTRY.register("pose", key -> new TransformConditionType<>(key, EntityPoseTransformCondition.CODEC));
    public static final Supplier<TransformConditionType<IsSittingTransformCondition>> IS_SITTING = REGISTRY.register("is_sitting", key -> new TransformConditionType<>(key, IsSittingTransformCondition.CODEC));
    public static final Supplier<TransformConditionType<IsBabyTransformCondition>> IS_BABY = REGISTRY.register("is_baby", key -> new TransformConditionType<>(key, IsBabyTransformCondition.CODEC));
}
