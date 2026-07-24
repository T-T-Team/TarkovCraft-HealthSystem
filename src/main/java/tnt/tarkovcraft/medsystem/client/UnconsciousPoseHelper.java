package tnt.tarkovcraft.medsystem.client;

import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.integration.MedSystemIntegrations;

public final class UnconsciousPoseHelper {

    public static boolean shouldApplyUnconsciousAttributes(LivingEntity entity) {
        return !entity.isPassenger() && BloodSystemManager.isUnconscious(entity) && MedSystemIntegrations.shouldAnimateUnconsciousMode();
    }
}
