package tnt.tarkovcraft.medsystem.api.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.ApiStatus;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteraction;

public abstract class EntityInteractionEvent extends Event {

    private final ItemStack itemStack;
    private final LivingEntity interactionEntity;
    private final LivingEntity originEntity;

    public EntityInteractionEvent(ItemStack itemStack, LivingEntity interactionEntity, LivingEntity originEntity) {
        this.itemStack = itemStack;
        this.interactionEntity = interactionEntity;
        this.originEntity = originEntity;
    }

    public boolean isSelfInteraction() {
        return interactionEntity == originEntity;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public LivingEntity getInteractionEntity() {
        return interactionEntity;
    }

    public LivingEntity getOriginEntity() {
        return originEntity;
    }

    public static final class CanInteract extends EntityInteractionEvent implements ICancellableEvent {

        public CanInteract(ItemStack itemStack, LivingEntity interactionEntity, LivingEntity originEntity) {
            super(itemStack, interactionEntity, originEntity);
        }
    }

    public static final class UnconsciousInteractionEvaluation extends EntityInteractionEvent {

        private final EntityInteraction interaction;
        private UserActionResult<Void> interactionResult = UserActionResult.successEmpty();

        public UnconsciousInteractionEvaluation(ItemStack itemStack, LivingEntity interactionEntity, LivingEntity originEntity, EntityInteraction interaction) {
            super(itemStack, interactionEntity, originEntity);
            this.interaction = interaction;
        }

        public EntityInteraction getInteraction() {
            return interaction;
        }

        public boolean isSuccessful() {
            return this.interactionResult.isSuccess();
        }

        public boolean isFailed() {
            return this.interactionResult.isFailure();
        }

        public void setFailure(Component reason) {
            this.interactionResult = UserActionResult.failure(reason);
        }

        @ApiStatus.Internal
        public UserActionResult<Void> getInteractionResult() {
            return this.interactionResult;
        }
    }

    public static final class UnconsciousInteractionFinished extends EntityInteractionEvent {

        private final EntityInteraction interaction;
        private final UserActionResult<Void> interactionResult;

        public UnconsciousInteractionFinished(ItemStack itemStack, LivingEntity interactionEntity, LivingEntity originEntity, EntityInteraction interaction, UserActionResult<Void> interactionResult) {
            super(itemStack, interactionEntity, originEntity);
            this.interaction = interaction;
            this.interactionResult = interactionResult;
        }

        public boolean wasSuccessful() {
            return this.interactionResult.isSuccess();
        }

        public boolean wasFailed() {
            return this.interactionResult.isFailure();
        }

        public boolean wasCancelled() {
            return this.wasFailed() && this.interactionResult == EntityInteraction.INTERACTION_CANCELLED;
        }

        public EntityInteraction getInteraction() {
            return interaction;
        }

        public UserActionResult<Void> getInteractionResult() {
            return interactionResult;
        }
    }
}
