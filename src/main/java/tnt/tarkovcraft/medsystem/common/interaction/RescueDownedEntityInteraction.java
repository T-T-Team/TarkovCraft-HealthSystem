package tnt.tarkovcraft.medsystem.common.interaction;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemEntityInteractions;

// TODO stop death countdown when rescue interaction is active
public final class RescueDownedEntityInteraction extends EntityInteraction {

    public static final RescueDownedEntityInteraction INSTANCE = new RescueDownedEntityInteraction();
    public static final MapCodec<RescueDownedEntityInteraction> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, RescueDownedEntityInteraction> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    private static final String ERR_UNABLE_TO_RESCUE = "unable_to_rescue";

    private RescueDownedEntityInteraction() {}

    @Override
    protected UserActionResult<Void> checkInteractionAvailability(Player origin, LivingEntity target) {
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(target);
        if (bloodSystem == null) {
            return this.createFailureResponse(ERR_UNABLE_TO_RESCUE);
        }
        UnconsciousOptions options = bloodSystem.getUnconsciousState().getUnconsciousOptions();
        if (!bloodSystem.isUnconscious() || !options.allowRescue()) {
            return this.createFailureResponse(ERR_UNABLE_TO_RESCUE);
        }
        return UserActionResult.successEmpty();
    }

    @Override
    protected void onInteractionFinished(Player origin, LivingEntity target) {
        if (!HealthSystem.hasCustomHealth(target) || !BloodSystemManager.isUnconscious(target))
            return;
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(target);
        bloodSystem.rescueDownedEntity(target);
        bloodSystem.synchronizeImmediately(target);
    }

    @Override
    public int getInteractionDuration() {
        return 200; // TODO configurable value
    }

    @Override
    public EntityInteractionType<?> type() {
        return MedSystemEntityInteractions.RESCUE_DOWNED_ENTITY.value();
    }
}
