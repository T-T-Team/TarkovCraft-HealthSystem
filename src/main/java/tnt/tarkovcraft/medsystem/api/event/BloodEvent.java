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

    public static abstract class BloodLossEvent extends BloodEvent {

        private final float originalAmount;

        public BloodLossEvent(LivingEntity entity, BloodData data, float originalAmount) {
            super(entity, data);
            this.originalAmount = originalAmount;
        }

        public final float getOriginalAmount() {
            return originalAmount;
        }

        public static final class Pre extends BloodLossEvent {

            private float amount;

            public Pre(LivingEntity entity, BloodData data, float originalAmount) {
                super(entity, data, originalAmount);
                this.amount = originalAmount;
            }

            public void setAmount(float amount) {
                this.amount = amount;
            }

            public float getAmount() {
                return amount;
            }
        }

        public static final class Post extends BloodLossEvent {

            private final float amount;

            public Post(LivingEntity entity, BloodData data, float originalAmount, float amount) {
                super(entity, data, originalAmount);
                this.amount = amount;
            }

            public float getAmount() {
                return amount;
            }
        }
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
