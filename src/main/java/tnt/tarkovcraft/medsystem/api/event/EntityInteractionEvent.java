package tnt.tarkovcraft.medsystem.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

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
}
