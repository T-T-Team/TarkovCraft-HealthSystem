package tnt.tarkovcraft.medsystem.integration;

import net.neoforged.fml.ModList;
import tnt.tarkovcraft.medsystem.integration.carryon.CarryOnIntegration;
import tnt.tarkovcraft.medsystem.integration.sable.SableIntegration;

public class MedSystemIntegrations {

    public static final String CARRY_ON = "carryon";
    public static final String SABLE = "sable_player_ragdoll";

    public static void setupIntegrations(ModList modList) {
        if (modList.isLoaded(CARRY_ON)) {
            CarryOnIntegration.initCommon();
        }
        if (modList.isLoaded(SABLE)) {
            SableIntegration.initCommon();
        }
    }
}
