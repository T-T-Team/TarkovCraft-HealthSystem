package tnt.tarkovcraft.medsystem.integration.sable;

import net.neoforged.neoforge.common.NeoForge;
import tnt.tarkovcraft.medsystem.MedicalSystem;

public class SableIntegration {

    public static void initCommon() {
        MedicalSystem.LOGGER.info("'Sable Ragdolls' mod detected, enabling integration");
        NeoForge.EVENT_BUS.register(new SableEventListener());
    }
}
