package tnt.tarkovcraft.medsystem.integration.carryon;

import net.neoforged.neoforge.common.NeoForge;

public class CarryOnIntegration {

    public static void initCommon() {
        NeoForge.EVENT_BUS.register(new CarryOnEventListener());
    }
}
