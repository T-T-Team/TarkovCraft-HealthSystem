package tnt.tarkovcraft.medsystem.common.interaction;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.network.message.C2S_RescueDownedEntity;

public final class RescueDownedEntityInteraction implements EntityInteraction {

    private static final Component NAME = EntityInteraction.createActionName(MedSystemConstants.MOD_ID, "rescue_downed_entity");
    private static final Component NOT_RESCUABLE = EntityInteraction.createValidationMessage(MedSystemConstants.MOD_ID, "rescue_downed_entity", "unable_to_rescue");

    @Override
    public UserActionResult<Void> evaluateValidity(Player origin, LivingEntity target) {
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(target);
        if (bloodSystem == null) {
            return UserActionResult.failure(NOT_RESCUABLE);
        }
        UnconsciousOptions options = bloodSystem.getUnconsciousState().getUnconsciousOptions();
        if (!bloodSystem.isUnconscious() || !options.allowRescue()) {
            return UserActionResult.failure(NOT_RESCUABLE);
        }
        return UserActionResult.successEmpty();
    }

    @Override
    public void onActionPerformed(Player origin, LivingEntity target) {
        ClientPacketDistributor.sendToServer(new C2S_RescueDownedEntity(target.getId()));
    }

    @Override
    public Component actionName() {
        return NAME;
    }

    @Override
    public int actionDuration() {
        return 100;
    }
}
