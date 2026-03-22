package tnt.tarkovcraft.medsystem.common.blood_system.assignment;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public interface BloodSystemListener {

    default void onBloodTick(float bloodVolume, LivingEntity entity, EntityBloodSystemDefinition definition) {}

    default void onIncompatibleBloodTransfusion(LivingEntity entity, ResourceLocation bloodType, ResourceLocation receivedBloodType, float receivedVolume) {}
}
