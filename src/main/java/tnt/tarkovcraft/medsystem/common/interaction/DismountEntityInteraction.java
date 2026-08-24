package tnt.tarkovcraft.medsystem.common.interaction;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.api.EntityInteraction;
import tnt.tarkovcraft.core.common.pose.EntityPoseManager;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousModeHelper;
import tnt.tarkovcraft.medsystem.common.init.MedSystemEntityInteractions;
import tnt.tarkovcraft.medsystem.common.pose.UnconsciousEntityPose;
import tnt.tarkovcraft.medsystem.util.HealthHelper;

public final class DismountEntityInteraction implements EntityInteraction {

    public static final DismountEntityInteraction INSTANCE = new DismountEntityInteraction();
    public static final MapCodec<DismountEntityInteraction> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, DismountEntityInteraction> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    private static final ResourceLocation IDENTIFIER = MedicalSystem.createIdentifier("dismount_entity");
    private static final String ERR_NOT_IN_VEHICLE = "not_in_vehicle";

    private DismountEntityInteraction() {}

    public static UserActionResult<Void> test(Context context) {
        LivingEntity target = context.target();
        if (!target.isPassenger()) {
            return UserActionResult.failure(Type.getErrorMessage(IDENTIFIER, ERR_NOT_IN_VEHICLE));
        }
        return UserActionResult.successEmpty();
    }

    @Override
    public void onCompleted(Context context) {
        LivingEntity target = context.target();
        HealthHelper.doWithEntityControlOverride(target, () -> {
            target.stopRiding();
            EntityPoseManager.setEntityPose(target, UnconsciousEntityPose.INSTANCE);
            UnconsciousModeHelper.updateEntityDimensions(target);
        });
    }

    @Override
    public void onFailed(Context context, InteractionResult reason) {
    }

    @Override
    public Type<?> type() {
        return MedSystemEntityInteractions.DISMOUNT_ENTITY.value();
    }
}
