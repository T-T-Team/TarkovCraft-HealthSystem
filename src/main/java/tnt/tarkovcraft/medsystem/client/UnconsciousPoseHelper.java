package tnt.tarkovcraft.medsystem.client;

import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

public final class UnconsciousPoseHelper {

    public static boolean shouldApplyUnconsciousAttributes(LivingEntity entity) {
        return !entity.isPassenger() && BloodSystem.isEntityUnconscious(entity) ;
    }
}
