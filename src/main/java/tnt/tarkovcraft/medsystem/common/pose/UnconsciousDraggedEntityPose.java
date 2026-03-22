package tnt.tarkovcraft.medsystem.common.pose;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.pose.EntityPose;
import tnt.tarkovcraft.core.common.pose.EntityPoseFlag;
import tnt.tarkovcraft.core.common.pose.EntityPoseType;
import tnt.tarkovcraft.core.common.pose.EntityStatusPose;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.init.MedSystemEntityPoses;

import java.util.Set;

public final class UnconsciousDraggedEntityPose implements EntityPose {

    public static final UnconsciousDraggedEntityPose INSTANCE = new UnconsciousDraggedEntityPose();
    public static final MapCodec<UnconsciousDraggedEntityPose> CODEC = MapCodec.unit(INSTANCE);

    private UnconsciousDraggedEntityPose() {}

    @Override
    public void onEnabled(LivingEntity livingEntity) {

    }

    @Override
    public EntityPose onDisabled(LivingEntity livingEntity) {
        return BloodSystemManager.isUnconscious(livingEntity) ? UnconsciousEntityPose.INSTANCE : null;
    }

    @Override
    public Set<EntityPoseFlag> getFlags() {
        return UnconsciousEntityPose.UNCONSCIOUS_FLAGS;
    }

    @Override
    public EntityPoseType<?> getType() {
        return MedSystemEntityPoses.UNCONSCIOUS_DRAGGED.value();
    }
}
