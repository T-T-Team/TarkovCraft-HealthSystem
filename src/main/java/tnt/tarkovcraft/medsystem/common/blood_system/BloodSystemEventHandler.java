package tnt.tarkovcraft.medsystem.common.blood_system;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class BloodSystemEventHandler {

    @SubscribeEvent
    private void onItemUse(PlayerInteractEvent.RightClickItem event) {
        this.cancelIfUnconscious(event);
    }

    @SubscribeEvent
    private void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        this.cancelIfUnconscious(event);
    }

    @SubscribeEvent
    private void onBlockAttack(PlayerInteractEvent.LeftClickBlock event) {
        this.cancelIfUnconscious(event);
    }

    @SubscribeEvent
    private void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        this.cancelIfUnconscious(event);
    }

    @SubscribeEvent
    private void onEntitySpecificInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        this.cancelIfUnconscious(event);
    }

    @SubscribeEvent
    private void onEntityKnockback(LivingKnockBackEvent event) {
        if (BloodSystemManager.isUnconscious(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private <T extends PlayerInteractEvent & ICancellableEvent> void cancelIfUnconscious(T event) {
        Player entity = event.getEntity();
        if (BloodSystemManager.isUnconscious(entity)) {
            event.setCanceled(true);
        }
    }
}
