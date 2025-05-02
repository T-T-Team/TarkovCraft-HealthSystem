package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import tnt.tarkovcraft.medsystem.MedicalSystem;

@Config(id = MedicalSystem.MOD_ID, filename = "medicalsystem")
public final class MedSystemConfig {

    @Configurable
    public boolean simpleArmorCalculation = false;

    @Configurable
    @Configurable.DecimalRange(min = 0.5, max = 5.0)
    @Configurable.Gui.Slider
    @Configurable.Gui.NumberFormat("0.000")
    public float explosionDamageScale = 1.75F;

    @Configurable
    @Configurable.DecimalRange(min = 0, max = 1.0)
    @Configurable.Gui.Slider
    @Configurable.Gui.NumberFormat("0.00")
    public float limbLossDeathCauseChance = 0.05F;
}
