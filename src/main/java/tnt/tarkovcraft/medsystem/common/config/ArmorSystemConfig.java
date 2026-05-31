package tnt.tarkovcraft.medsystem.common.config;

import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.UpdateRestrictions;
import tnt.tarkovcraft.medsystem.common.armor.ArmorSystem;

public class ArmorSystemConfig {

    @Configurable
    @Configurable.Comment(value = {
            "Defines armor calculation logic with custom health system",
            "SIMULATED - modular armor with fully simulated logic such as deflections, blunt damage and so on",
            "MODULAR - modular armor - getting entryPoint in head damages only helmet",
            "MODULAR_BOOSTED - same as above, but each armor piece has additional protection. Configurable below",
            "VANILLA - vanilla armor calculation, full armor is used for any damage calculations"
    }, localize = true)
    @Configurable.UpdateRestriction(UpdateRestrictions.MAIN_MENU)
    public ArmorSystem armorSystem = ArmorSystem.SIMULATED;

    @Configurable
    @Configurable.Synchronized
    @Configurable.DecimalRange(min = 0.0)
    @Configurable.Gui.NumberFormat("0.000")
    @Configurable.Comment(value = "Additional armor protection per armor piece when using MODULAR_BOOSTED armor system", localize = true)
    public float modularBoostedArmorMultiplier = 2.5F;
}
