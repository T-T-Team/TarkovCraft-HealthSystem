package tnt.tarkovcraft.medsystem.common.interaction;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.medsystem.api.event.EntityInteractionEvent;
import tnt.tarkovcraft.medsystem.api.event.RegisterUnconsciousInteractionsEvent;

import java.util.ArrayList;
import java.util.List;

public final class EntityInteractions {

    public static final RescueDownedEntityInteraction RESCUE_DOWNED = new RescueDownedEntityInteraction();

    private static List<EntityInteraction> interactionList;

    @ApiStatus.Internal
    public static void init() {
        List<EntityInteraction> interactions = new ArrayList<>();
        interactions.add(RESCUE_DOWNED);

        List<EntityInteraction> externalInteractions = new ArrayList<>();
        ModLoader.postEvent(new RegisterUnconsciousInteractionsEvent(externalInteractions));
        interactions.addAll(externalInteractions);

        interactionList = ImmutableList.copyOf(interactions);
    }

    public static UserActionResult<Void> evaluateInteraction(Player player, LivingEntity entity, EntityInteraction interaction) {
        double distanceSq = player.distanceToSqr(entity);
        if (distanceSq > interaction.maxDistanceSqr()) {
            return UserActionResult.failure(EntityInteraction.ENTITY_TOO_FAR);
        }
        EntityInteractionEvent.UnconsciousInteractionEvaluation evaluationEvent = NeoForge.EVENT_BUS.post(new EntityInteractionEvent.UnconsciousInteractionEvaluation(player.getMainHandItem(), player, entity, interaction));
        if (!evaluationEvent.isSuccessful()) {
            return evaluationEvent.getInteractionResult();
        }
        return interaction.evaluateValidity(player, entity);
    }

    public static void onInteractionCompletedCallback(Player player, LivingEntity entity, EntityInteraction interaction) {
        NeoForge.EVENT_BUS.post(new EntityInteractionEvent.UnconsciousInteractionFinished(player.getMainHandItem(), player, entity, interaction));
    }

    public static List<EntityInteraction> getInteractions() {
        return interactionList;
    }
}
