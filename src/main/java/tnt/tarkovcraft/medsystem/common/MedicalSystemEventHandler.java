package tnt.tarkovcraft.medsystem.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import tnt.tarkovcraft.medsystem.MedicalSystem;

public final class MedicalSystemEventHandler {

    @SubscribeEvent
    private void onEntitySpawn(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.isCanceled())
            return;
        if (entity instanceof LivingEntity livingEntity) {
            MedicalSystem.HEALTH_SYSTEM.getHealthContainer(livingEntity)
                    .ifPresent(container -> container.bind(livingEntity));
        }
    }
}
