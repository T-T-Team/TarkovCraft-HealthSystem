package tnt.tarkovcraft.medsystem.common.blood_system.assignment;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

public interface BloodSystemListener {

    default void onBloodTick(float bloodVolume, LivingEntity entity, EntityBloodSystemDefinition definition) {}

    default void onIncompatibleBloodTransfusion(LivingEntity entity, Identifier bloodType, Identifier receivedBloodType, float receivedVolume) {}
}
