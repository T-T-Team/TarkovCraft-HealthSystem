package tnt.tarkovcraft.medsystem.common.pose;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.common.pose.EntityPoseFlag;
import tnt.tarkovcraft.core.common.pose.EntityPoseType;
import tnt.tarkovcraft.core.common.pose.EntityStatusPose;
import tnt.tarkovcraft.medsystem.common.init.MedSystemEntityPoses;

import java.util.Set;

public final class UnconsciousSittingEntityPose extends EntityStatusPose {

    public static final UnconsciousSittingEntityPose INSTANCE = new UnconsciousSittingEntityPose();
    public static final MapCodec<UnconsciousSittingEntityPose> CODEC = MapCodec.unit(INSTANCE);

    private UnconsciousSittingEntityPose() {}

    @Override
    public Set<EntityPoseFlag> getFlags() {
        return UnconsciousEntityPose.UNCONSCIOUS_FLAGS;
    }

    @Override
    public EntityPoseType<?> getType() {
        return MedSystemEntityPoses.UNCONSCIOUS_SITTING.value();
    }
}
