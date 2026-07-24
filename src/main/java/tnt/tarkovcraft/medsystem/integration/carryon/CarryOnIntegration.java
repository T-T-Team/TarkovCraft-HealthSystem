package tnt.tarkovcraft.medsystem.integration.carryon;

import net.neoforged.neoforge.common.NeoForge;
import tnt.tarkovcraft.medsystem.MedicalSystem;

public class CarryOnIntegration {

    public static void initCommon() {
        MedicalSystem.LOGGER.info("'Carry On' mod detected, enabling integration");
        NeoForge.EVENT_BUS.register(new CarryOnEventListener());
    }
}
