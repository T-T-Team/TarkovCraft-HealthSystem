package tnt.tarkovcraft.medsystem.common.armor;

import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;

public class ModularBoostedArmorComponent extends ModularArmorComponent {

    public static final ModularBoostedArmorComponent INSTANCE = new ModularBoostedArmorComponent();

    @Override
    protected float getArmorValueMultiplier() {
        MedSystemConfig config = MedicalSystem.getConfig();
        return config.armor.modularBoostedArmorMultiplier;
    }
}
