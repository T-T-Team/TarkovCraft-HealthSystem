package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.core.common.init.CoreRegistries;
import tnt.tarkovcraft.core.common.pose.EntityPoseType;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.pose.UnconsciousDraggedEntityPose;
import tnt.tarkovcraft.medsystem.common.pose.UnconsciousEntityPose;
import tnt.tarkovcraft.medsystem.common.pose.UnconsciousSittingEntityPose;

public final class MedSystemEntityPoses {

    public static final DeferredRegister<EntityPoseType<?>> REGISTRY = DeferredRegister.create(CoreRegistries.Keys.ENTITY_POSE, MedSystemConstants.MOD_ID);

    public static final Holder<EntityPoseType<?>> UNCONSCIOUS = REGISTRY.register("unconscious", key -> new EntityPoseType<>(key, UnconsciousEntityPose.CODEC));
    public static final Holder<EntityPoseType<?>> UNCONSCIOUS_SITTING = REGISTRY.register("unconscious_sitting", key -> new EntityPoseType<>(key, UnconsciousSittingEntityPose.CODEC));
    public static final Holder<EntityPoseType<?>> UNCONSCIOUS_DRAGGED = REGISTRY.register("unconscious_dragged", key -> new EntityPoseType<>(key, UnconsciousDraggedEntityPose.CODEC));
}
