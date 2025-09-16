package tnt.tarkovcraft.medsystem.common.status;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class BloodSystemEventHandler {

    @SubscribeEvent
    private void onPlayerPostTick(PlayerTickEvent.Post event) {
        if (!BloodSystem.isEnabled())
            return;
        Player player = event.getEntity();
        BloodData data = BloodSystem.getBloodData(player);
        data.update(player);
    }

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

    private <T extends PlayerInteractEvent & ICancellableEvent> void cancelIfUnconscious(T event) {
        Player entity = event.getEntity();
        if (BloodSystem.isEntityUnconscious(entity)) {
            event.setCanceled(true);
        }
    }
}
