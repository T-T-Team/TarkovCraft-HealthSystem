package tnt.tarkovcraft.medsystem.common.interaction;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;

// TODO refactor to normal entity interactions not tied to unconscious system
public interface EntityInteraction {

    Component ENTITY_TOO_FAR = createValidationMessage(MedSystemConstants.MOD_ID, "shared", "entity_too_far");
    int MAX_DISTANCE_SQR = 16;

    /**
     * Checks if the action can be performed on the target entity. Evaluated on both sides.
     * @param origin Interaction entity
     * @param target Interaction target
     * @return Action result either with an error message or empty result
     */
    UserActionResult<Void> evaluateValidity(Player origin, LivingEntity target);

    /**
     * Called when action is performed. Always called only on the client side
     * @param origin Interaction entity
     * @param target Interaction target
     */
    void onActionPerformed(Player origin, LivingEntity target);

    Component actionName();

    int actionDuration();

    default int maxDistanceSqr() {
        return MAX_DISTANCE_SQR;
    }

    default boolean is(EntityInteraction other) {
        return other == this;
    }

    static Component createActionName(String namespace, String name) {
        return Component.translatable("entity_interaction." + namespace + "." + name);
    }

    static Component createValidationMessage(String namespace, String name, String error) {
        return Component.translatable("entity_interaction." + namespace + "." + name + ".response." + error);
    }
}
