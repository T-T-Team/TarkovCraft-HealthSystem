package tnt.tarkovcraft.medsystem.common.interaction;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.medsystem.api.event.EntityInteractionEvent;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

public abstract class EntityInteraction {

    public static final Codec<EntityInteraction> CODEC = MedSystemRegistries.ENTITY_INTERACTION.byNameCodec().dispatch(EntityInteraction::type, EntityInteractionType::codec);
    public static final UserActionResult<Void> ENTITY_TOO_FAR = UserActionResult.failure(EntityInteractionType.getErrorMessage(EntityInteractionType.SHARED_ERROR_IDENTIFIER, "entity_too_far"));
    public static final UserActionResult<Void> INTERACTION_CANCELLED = UserActionResult.failure(EntityInteractionType.getErrorMessage(EntityInteractionType.SHARED_ERROR_IDENTIFIER, "cancelled"));
    public static final int MAX_DISTANCE_SQR = 16;

    public abstract EntityInteractionType<?> type();

    protected abstract UserActionResult<Void> checkInteractionAvailability(Player origin, LivingEntity target);

    protected abstract void onInteractionFinished(Player origin, LivingEntity target);

    protected void onInteractionFailedOrCancelled(Player origin, LivingEntity target) {
    }

    public abstract int getInteractionDuration();

    protected int maxDistanceSqr() {
        return MAX_DISTANCE_SQR;
    }

    public final Component getDisplayName() {
        return this.type().getDisplayName();
    }

    public final UserActionResult<Void> checkAvailability(Player origin, LivingEntity target) {
        double distanceSq = origin.distanceToSqr(target);
        if (distanceSq > this.maxDistanceSqr()) {
            return ENTITY_TOO_FAR;
        }
        ItemStack interactionItem = origin.getMainHandItem();
        EntityInteractionEvent.UnconsciousInteractionEvaluation evaluationEvent = NeoForge.EVENT_BUS.post(new EntityInteractionEvent.UnconsciousInteractionEvaluation(interactionItem, origin, target, this));
        if (!evaluationEvent.isSuccessful()) {
            return evaluationEvent.getInteractionResult();
        }
        return this.checkInteractionAvailability(origin, target);
    }

    public final void finishInteraction(Player origin, LivingEntity target) {
        UserActionResult<Void> result = this.checkAvailability(origin, target);
        if (result.isSuccess()) {
            this.onInteractionFinished(origin, target);
        } else {
            this.onInteractionFailedOrCancelled(origin, target);
        }
        NeoForge.EVENT_BUS.post(new EntityInteractionEvent.UnconsciousInteractionFinished(origin.getMainHandItem(), origin, target, this, result));
    }

    public final void cancelInteraction(Player origin, LivingEntity target) {
        this.onInteractionFailedOrCancelled(origin, target);
        NeoForge.EVENT_BUS.post(new EntityInteractionEvent.UnconsciousInteractionFinished(origin.getMainHandItem(), origin, target, this, INTERACTION_CANCELLED));
    }

    protected final UserActionResult<Void> createFailureResponse(String code, Object... args) {
        Component message = this.type().getValidationErrorMessage(code, args);
        return UserActionResult.failure(message);
    }
}
