package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import tnt.tarkovcraft.medsystem.MedicalSystem;

@Config(id = MedicalSystem.MOD_ID, filename = "medicalsystem")
public final class MedSystemConfig {

    @Configurable
    @Configurable.Comment(localize = true, value = "Includes all equipped armors for damage reduction calculation")
    public boolean simpleArmorCalculation = false;

    @Configurable
    @Configurable.DecimalRange(min = 0.15, max = 3.0)
    @Configurable.Gui.Slider
    @Configurable.Gui.NumberFormat("0.00#")
    @Configurable.Comment(localize = true, value = "Damage scale for explosions")
    public float explosionDamageScale = 0.6F;

    @Configurable
    @Configurable.DecimalRange(min = 0, max = 1.0)
    @Configurable.Gui.Slider
    @Configurable.Gui.NumberFormat("0.00#")
    @Configurable.Comment(localize = true, value = "Losing limb has small chance to cause immediate death")
    public float limbLossDeathCauseChance = 0.05F;

    @Configurable
    @Configurable.Comment(localize = true, value = "Health will be primarily recovered into vital parts")
    public boolean prioritizeVitalHealing = true;

    @Configurable
    @Configurable.DecimalRange(min = 0, max = 1.0)
    @Configurable.Gui.Slider
    @Configurable.Gui.NumberFormat("0.0##")
    @Configurable.Comment(localize = true, value = "Threshold for prioritized vital body part health recovery")
    public float vitalBodyPartHealthTrigger = 0.75F;
}
