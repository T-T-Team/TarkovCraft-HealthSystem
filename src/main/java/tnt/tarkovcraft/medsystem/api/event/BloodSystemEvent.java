package tnt.tarkovcraft.medsystem.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;

public abstract class BloodSystemEvent extends Event {

    protected final LivingEntity entity;
    protected final EntityBloodSystem bloodSystem;

    public BloodSystemEvent(LivingEntity entity, EntityBloodSystem bloodSystem) {
        this.entity = entity;
        this.bloodSystem = bloodSystem;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public EntityBloodSystem getBloodSystem() {
        return bloodSystem;
    }

    public static final class UnconsciousStart extends BloodSystemEvent {

        public UnconsciousStart(LivingEntity entity, EntityBloodSystem bloodSystem) {
            super(entity, bloodSystem);
        }
    }

    public static final class UnconsciousEnd extends BloodSystemEvent {

        public UnconsciousEnd(LivingEntity entity, EntityBloodSystem bloodSystem) {
            super(entity, bloodSystem);
        }
    }

    public static final class EntityRescueAttempt extends BloodSystemEvent {

        private final LivingEntity rescuer;
        private final ItemStack itemStack;
        private boolean canRescue;

        public EntityRescueAttempt(LivingEntity entity, EntityBloodSystem bloodSystem, LivingEntity rescuer, ItemStack itemStack) {
            super(entity, bloodSystem);
            this.rescuer = rescuer;
            this.itemStack = itemStack;
            this.canRescue = true;
        }

        public void setRescueResult(boolean canRescue) {
            this.canRescue = canRescue;
        }

        public boolean canRescue() {
            return this.canRescue;
        }

        public LivingEntity getRescuer() {
            return rescuer;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }
    }

    public static final class EntityRescued extends BloodSystemEvent {

        private final LivingEntity rescuer;
        private final ItemStack itemStack;

        public EntityRescued(LivingEntity entity, EntityBloodSystem bloodSystem, LivingEntity rescuer, ItemStack itemStack) {
            super(entity, bloodSystem);
            this.rescuer = rescuer;
            this.itemStack = itemStack;
        }

        public LivingEntity getRescuer() {
            return rescuer;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }
    }
}
