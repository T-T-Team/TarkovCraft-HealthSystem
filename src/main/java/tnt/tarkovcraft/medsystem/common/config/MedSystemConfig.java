package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import tnt.tarkovcraft.medsystem.MedicalSystem;

@Config(id = MedicalSystem.MOD_ID, filename = "medicalsystem")
public final class MedSystemConfig {

    @Configurable
    public boolean simpleArmorCalculation = false;
}
