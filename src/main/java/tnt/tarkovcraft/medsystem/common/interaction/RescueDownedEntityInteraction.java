package tnt.tarkovcraft.medsystem.common.interaction;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.api.EntityInteraction;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemEntityInteractions;

// TODO stop death countdown when rescue interaction is active
public final class RescueDownedEntityInteraction implements EntityInteraction {

    public static final RescueDownedEntityInteraction INSTANCE = new RescueDownedEntityInteraction();
    public static final MapCodec<RescueDownedEntityInteraction> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, RescueDownedEntityInteraction> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    private static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("rescue_downed_entity");
    private static final String ERR_UNABLE_TO_RESCUE = "unable_to_rescue";

    private RescueDownedEntityInteraction() {}

    public static UserActionResult<Void> test(Context context) {
        LivingEntity target = context.target();
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(target);
        if (bloodSystem == null) {
            return UserActionResult.failure(Type.getErrorMessage(IDENTIFIER, ERR_UNABLE_TO_RESCUE));
        }
        UnconsciousOptions options = bloodSystem.getUnconsciousState().getUnconsciousOptions();
        if (!bloodSystem.isUnconscious() || !options.allowRescue()) {
            return UserActionResult.failure(Type.getErrorMessage(IDENTIFIER, ERR_UNABLE_TO_RESCUE));
        }
        return UserActionResult.successEmpty();
    }

    @Override
    public void onCompleted(Context context) {
        LivingEntity target = context.target();
        if (!HealthSystem.hasCustomHealth(target) || !BloodSystemManager.isUnconscious(target))
            return;
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(target);
        bloodSystem.rescueDownedEntity(target);
        bloodSystem.synchronizeImmediately(target);
    }

    @Override
    public void onFailed(Context context, InteractionResult reason) {
    }

    @Override
    public Type<?> type() {
        return MedSystemEntityInteractions.RESCUE_DOWNED_ENTITY.value();
    }
}
