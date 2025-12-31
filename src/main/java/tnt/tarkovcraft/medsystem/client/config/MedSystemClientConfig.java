package tnt.tarkovcraft.medsystem.client.config;

import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import tnt.tarkovcraft.core.util.HorizontalAlignment;
import tnt.tarkovcraft.core.util.VerticalAlignment;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;

@Config(id = MedSystemConstants.MOD_ID + "-client", filename = "medicalsystem-client", group = MedSystemConstants.MOD_ID)
public final class MedSystemClientConfig {

    @Configurable
    public HealthOverlayConfiguration healthOverlay = new HealthOverlayConfiguration(true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, 0, 0);

    @Configurable
    @Configurable.Comment("Allows you to toggle default health HUD overlay")
    public boolean renderHealth = true;

    @Configurable
    @Configurable.Comment("Set limb health detail render mode")
    public HealthDisplayType healthDisplayType = HealthDisplayType.HEARTS;

    @Configurable
    @Configurable.Range(min = 0, max = 2)
    @Configurable.DependsOn(configValues = @Configurable.DependsOn.ConfigValue(location = "medsystem-client:healthDisplayType", accepts = "NUMERIC"))
    @Configurable.Gui.Slider
    public int numericHealthScale = 1;

    @Configurable
    public BloodDecalConfig bloodDecals = new BloodDecalConfig();
}
