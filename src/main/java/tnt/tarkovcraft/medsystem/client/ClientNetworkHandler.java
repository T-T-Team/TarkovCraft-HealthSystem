package tnt.tarkovcraft.medsystem.client;

import net.minecraft.client.Minecraft;
import tnt.tarkovcraft.medsystem.client.screen.SelectLimbScreen;

public final class ClientNetworkHandler {

    public static void openLimbSelectionScreen(boolean selfHealing, int entityID) {
        Minecraft client = Minecraft.getInstance();
        client.gui.setScreen(new SelectLimbScreen(selfHealing, entityID));
    }
}
