package tnt.tarkovcraft.medsystem.common.status;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.BloodEvent;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;

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
        BloodEvent.BloodLossEvent.Pre event = NeoForge.EVENT_BUS.post(new BloodEvent.BloodLossEvent.Pre(entity, data, amount));
        float eventAmount = event.getAmount();
        if (eventAmount > 0) {
            float newVolume = data.getBloodVolume() - eventAmount;
            data.setBloodVolume(newVolume);
            NeoForge.EVENT_BUS.post(new BloodEvent.BloodLossEvent.Post(entity, data, amount, eventAmount));
            data.updateEffects(entity);
            data.sync(entity);
        }
    }

    public static BloodData getBloodData(LivingEntity entity) {
        return entity.getData(BLOOD_DATA);
    }

    public static boolean isEntityUnconscious(LivingEntity entity) {
        return entity.isAlive() && hasBloodDataIntegration(entity) && getBloodData(entity).isUnconscious();
    }

    public static boolean canGiveUp(Player player) {
        if (player == null)
            return false;
        BloodData bloodData = getBloodData(player);
        if (bloodData.isUnconscious()) {
            BloodData.UnconsciousInfo info = bloodData.getUnconsciousInfo();
            float percentage = bloodData.getBloodVolumePercentage();
            BloodStatus status = BloodStatus.fromBloodLevelPercentage(percentage);
            return info.showGiveUpHint() && status != BloodStatus.DEATH;
        }
        return false;
    }
}
