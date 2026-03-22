package tnt.tarkovcraft.medsystem.common.pose;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.common.pose.CoreEntityPoseFlags;
import tnt.tarkovcraft.core.common.pose.EntityPoseFlag;
import tnt.tarkovcraft.core.common.pose.EntityPoseType;
import tnt.tarkovcraft.core.common.pose.EntityStatusPose;
import tnt.tarkovcraft.medsystem.common.init.MedSystemEntityPoses;

import java.util.Set;

public final class UnconsciousEntityPose extends EntityStatusPose {

    public static final UnconsciousEntityPose INSTANCE = new UnconsciousEntityPose();
    public static final MapCodec<UnconsciousEntityPose> CODEC = MapCodec.unit(INSTANCE);
    public static final Set<EntityPoseFlag> UNCONSCIOUS_FLAGS = Set.of(
            CoreEntityPoseFlags.NO_INTERACTION,
            CoreEntityPoseFlags.NO_MOVEMENT,
            CoreEntityPoseFlags.NO_KNOCKBACK
    );

    private UnconsciousEntityPose() {}

    @Override
    public Set<EntityPoseFlag> getFlags() {
        return UNCONSCIOUS_FLAGS;
    }

    @Override
    public EntityPoseType<?> getType() {
        return MedSystemEntityPoses.UNCONSCIOUS.value();
    }
}
