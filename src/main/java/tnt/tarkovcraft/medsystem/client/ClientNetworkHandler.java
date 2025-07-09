package tnt.tarkovcraft.medsystem.client;

import net.minecraft.client.Minecraft;
import tnt.tarkovcraft.medsystem.client.screen.SelectBodyPartScreen;

public final class ClientNetworkHandler {

    public static void openBodyPartSelectionScreen() {
        Minecraft client = Minecraft.getInstance();
        client.setScreen(new SelectBodyPartScreen());
    }
}
