package tnt.tarkovcraft.medsystem.common.interaction;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import tnt.tarkovcraft.core.common.pose.EntityPoseManager;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousModeHelper;
import tnt.tarkovcraft.medsystem.common.init.MedSystemEntityInteractions;
import tnt.tarkovcraft.medsystem.common.pose.UnconsciousEntityPose;
import tnt.tarkovcraft.medsystem.util.HealthHelper;

public final class DismountEntityInteraction extends EntityInteraction {

    public static final DismountEntityInteraction INSTANCE = new DismountEntityInteraction();
    public static final MapCodec<DismountEntityInteraction> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, DismountEntityInteraction> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    private static final String ERR_NOT_IN_VEHICLE = "not_in_vehicle";

    private DismountEntityInteraction() {}

    @Override
    protected UserActionResult<Void> checkInteractionAvailability(Player origin, LivingEntity target) {
        if (!target.isPassenger()) {
            return this.createFailureResponse(ERR_NOT_IN_VEHICLE);
        }
        return UserActionResult.successEmpty();
    }

    @Override
    protected void onInteractionFinished(Player origin, LivingEntity target) {
        HealthHelper.doWithEntityControlOverride(target, () -> {
            target.stopRiding();
            EntityPoseManager.setEntityPose(target, UnconsciousEntityPose.INSTANCE);
            UnconsciousModeHelper.updateEntityDimensions(target);
        });
    }

    @Override
    public int getInteractionDuration() {
        return 60; // TODO configurable value
    }

    @Override
    public EntityInteractionType<?> type() {
        return MedSystemEntityInteractions.DISMOUNT_ENTITY.value();
    }
}
