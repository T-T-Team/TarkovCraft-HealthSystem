package tnt.tarkovcraft.medsystem.common.status;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

public enum BloodStatus {

    DEATH(0.25F, BloodData::onDeathBloodLevel), // 1.25L
    UNCONSCIOUS(0.40F, BloodData::onUnconsciousBloodLevel), // 2.0L
    RANDOM_BLACKOUT(0.65F, BloodData::onRandomBlackoutBloodLevel), // 3.25L
    MODERATE_BLOOD_LOSS(0.80F, BloodData::onModerateBloodLoss), // 4.0L
    MILD_BLOOD_LOSS(0.90F, BloodData::onMildBloodLoss), // 4.5L
    HEALTHY(1.0F, BloodData::onClearDebuffData);

    private final float percentage;
    private final DebuffEffect effect;

    BloodStatus(float percentage, DebuffEffect effect) {
        this.percentage = percentage;
        this.effect = effect;
    }

    public float getAmount() {
        return this.percentage;
    }

    public boolean isInRange(float percentage) {
        return percentage < this.percentage;
    }

    public boolean isLowBloodLevel() {
        return this != HEALTHY;
    }

    public boolean isSameOrAbove(BloodStatus status) {
        return this.ordinal() >= status.ordinal();
    }

    public boolean isSameOrBelow(BloodStatus status) {
        return this.ordinal() <= status.ordinal();
    }

    public void applyEffects(BloodData bloodData, LivingEntity entity, ServerLevel level, HealthContainer container) {
        this.effect.apply(bloodData, entity, level, container);
    }

    public static BloodStatus fromBloodLevelPercentage(float percentage) {
        for (BloodStatus bloodStatus : BloodStatus.values()) {
            if (bloodStatus.isInRange(percentage)) {
                return bloodStatus;
            }
        }
        return HEALTHY;
    }

    @FunctionalInterface
    public interface DebuffEffect {

        void apply(BloodData bloodData, LivingEntity entity, ServerLevel level, HealthContainer container);
    }
}
