package tnt.tarkovcraft.medsystem.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public abstract class InteractableItemEvent extends Event {

    private final LivingEntity origin;
    private final LivingEntity target;
    private final ItemStack itemStack;

    public InteractableItemEvent(LivingEntity origin, LivingEntity target, ItemStack itemStack) {
        this.origin = origin;
        this.target = target;
        this.itemStack = itemStack;
    }

    public LivingEntity getOriginEntity() {
        return origin;
    }

    public LivingEntity getTarget() {
        return target;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public static final class CanInteract extends InteractableItemEvent implements ICancellableEvent {

        public CanInteract(LivingEntity origin, LivingEntity target, ItemStack itemStack) {
            super(origin, target, itemStack);
        }
    }
}
