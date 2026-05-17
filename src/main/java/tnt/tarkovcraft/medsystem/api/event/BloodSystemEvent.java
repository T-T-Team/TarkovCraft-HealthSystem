package tnt.tarkovcraft.medsystem.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;

// TODO more events, shock data
public abstract class BloodSystemEvent extends Event {

    private final LivingEntity entity;
    private final EntityBloodSystem bloodSystem;

    public BloodSystemEvent(LivingEntity entity, EntityBloodSystem bloodSystem) {
        this.entity = entity;
        this.bloodSystem = bloodSystem;
    }

    public LivingEntity getEntity() {
        return this.entity;
    }

    public EntityBloodSystem getBloodSystem() {
        return this.bloodSystem;
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
}
