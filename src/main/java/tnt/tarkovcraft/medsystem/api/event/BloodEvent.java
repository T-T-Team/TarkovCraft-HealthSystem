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

    public static final class BloodEffectsTick extends BloodEvent {

        private final BloodStatus status;
        private final float bloodVolumePercentage;

        public BloodEffectsTick(LivingEntity entity, BloodData data, BloodStatus status, float bloodVolumePercentage) {
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

    public static final class OnWakeUp extends BloodEvent {

        private Integer unconscious;

        public OnWakeUp(LivingEntity entity, BloodData data) {
            super(entity, data);
        }

        public boolean willWakeUp() {
            return unconscious == null || unconscious <= 0;
        }

        public void forceWakeUp() {
            this.unconscious = null;
        }

        public void cancelWakingUp(int newUnconsciousTime) {
            this.unconscious = newUnconsciousTime;
        }

        public Integer getUnconsciousTime() {
            return unconscious;
        }
    }
}
