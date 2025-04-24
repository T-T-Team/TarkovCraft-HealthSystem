package tnt.tarkovcraft.medsystem.common.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.transform.*;

import java.util.function.Supplier;

public final class MedSystemHitboxTransforms {

    public static final DeferredRegister<EntityHitboxTransformType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.TRANSFORM, MedicalSystem.MOD_ID);

    public static final Supplier<EntityHitboxTransformType<MoveTransform>> MOVE = REGISTRY.register("move", key -> new EntityHitboxTransformType<>(key, MoveTransform.CODEC));
    public static final Supplier<EntityHitboxTransformType<ScaleHitboxTransform>> SCALE = REGISTRY.register("scale", key -> new EntityHitboxTransformType<>(key, ScaleHitboxTransform.CODEC));
    public static final Supplier<EntityHitboxTransformType<RotateTransform>> ROTATE = REGISTRY.register("rotate", key -> new EntityHitboxTransformType<>(key, RotateTransform.CODEC));
    public static final Supplier<EntityHitboxTransformType<ResizeTransform>> RESIZE = REGISTRY.register("resize", key -> new EntityHitboxTransformType<>(key, ResizeTransform.CODEC));
    public static final Supplier<EntityHitboxTransformType<ApplyEntityYawBodyRotationTransform>> ENTITY_BODY_YAW = REGISTRY.register("entity_body_yaw", key -> new EntityHitboxTransformType<>(key, ApplyEntityYawBodyRotationTransform.CODEC));
    public static final Supplier<EntityHitboxTransformType<ApplyHeadRotationTransform>> ENTITY_HEAD_ROTATION = REGISTRY.register("entity_head_rotation", key -> new EntityHitboxTransformType<>(key, ApplyHeadRotationTransform.CODEC));
}
