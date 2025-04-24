package tnt.tarkovcraft.medsystem.common.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.transform.EntityPoseTransformCondition;
import tnt.tarkovcraft.medsystem.common.health.transform.IsSittingTransformCondition;
import tnt.tarkovcraft.medsystem.common.health.transform.TransformConditionType;

import java.util.function.Supplier;

public final class MedSystemTransformConditions {

    public static final DeferredRegister<TransformConditionType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.TRANSFORM_CONDITION, MedicalSystem.MOD_ID);

    public static final Supplier<TransformConditionType<EntityPoseTransformCondition>> ENTITY_POSE = REGISTRY.register("pose", key -> new TransformConditionType<>(key, EntityPoseTransformCondition.CODEC));
    public static final Supplier<TransformConditionType<IsSittingTransformCondition>> IS_SITTING = REGISTRY.register("is_sitting", key -> new TransformConditionType<>(key, IsSittingTransformCondition.CODEC));
}
