package tnt.tarkovcraft.medsystem.integration.carryon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import tnt.tarkovcraft.medsystem.api.event.BloodSystemEvent;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;
import tschipp.carryon.common.carry.PlacementHandler;
import tschipp.carryon.events.EntityPickupEvent;

public final class CarryOnEventListener {

    @SubscribeEvent
    private void onEntityPickup(EntityPickupEvent event) {
        Entity target = event.target;
        if (BloodSystemManager.isUnconscious(event.player)) {
            event.setCanceled(true);
            return;
        }
        if (target instanceof LivingEntity entity && BloodSystemManager.isUnconscious(entity)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    private void onEnterUnconsciousMode(BloodSystemEvent.UnconsciousStart event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        CarryOnData carryOnData = CarryOnDataManager.getCarryData(player);
        if (carryOnData.isCarrying()) {
            PlacementHandler.placeCarried(player);
            player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
    }
}
