package tnt.tarkovcraft.medsystem.common.status;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

import static tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments.BLOOD_DATA;

public final class BloodSystem {

    public static boolean isEnabled() {
        MedSystemConfig config = MedicalSystem.getConfig();
        return config.useBloodSystem;
    }

    public static boolean hasBloodDataIntegration(LivingEntity entity) {
        return isEnabled() && entity.hasData(BLOOD_DATA);
    }

    public static void causeBloodLoss(LivingEntity entity, float amount) {
        if (!isEnabled() && !hasBloodDataIntegration(entity)) {
            return;
        }
        BloodData data = getBloodData(entity);
        float volume = data.getBloodVolume();
        float newVolume = volume - Mth.abs(amount);
        data.setBloodVolume(newVolume);
        data.updateEffects(entity);
        data.sync(entity);
    }

    public static BloodData getBloodData(LivingEntity entity) {
        return entity.getData(BLOOD_DATA);
    }

    public static boolean isEntityUnconscious(LivingEntity entity) {
        return entity.isAlive() && getBloodData(entity).isUnconscious();
    }

    public static float getBloodVolume(LivingEntity entity) {
        return getBloodData(entity).getBloodVolume();
    }

    public static boolean isModerateBloodLoss(LivingEntity entity) {
        return getBloodVolume(entity) < BloodData.MODERATE_BLOOD_LOSS;
    }

    public static void setUnconscious(LivingEntity entity, int unconsciousDuration) {
        getBloodData(entity).setUnconsciousTime(unconsciousDuration);
    }

    public static void setUnconscious(LivingEntity entity, TickValue unconsciousDuration) {
        setUnconscious(entity, unconsciousDuration.tickValue());
    }

    public static void sync(LivingEntity entity) {
        if (entity.isAlive())
            entity.syncData(MedSystemDataAttachments.BLOOD_DATA);
    }
}
