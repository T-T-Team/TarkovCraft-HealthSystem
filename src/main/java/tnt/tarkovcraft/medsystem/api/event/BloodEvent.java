package tnt.tarkovcraft.medsystem.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodStatus;

public abstract class BloodEvent extends Event {

    private final LivingEntity entity;
    private final BloodData data;

    public BloodEvent(LivingEntity entity, BloodData data) {
        this.entity = entity;
        this.data = data;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public BloodData getData() {
        return data;
    }

    public static final class EffectUpdating extends BloodEvent {

        private final BloodStatus status;
        private final float bloodVolumePercentage;

        public EffectUpdating(LivingEntity entity, BloodData data, BloodStatus status, float bloodVolumePercentage) {
            super(entity, data);
            this.status = status;
            this.bloodVolumePercentage = bloodVolumePercentage;
        }

        public BloodStatus getStatus() {
            return status;
        }

        public float getBloodVolumePercentage() {
            return bloodVolumePercentage;
        }
    }
}
