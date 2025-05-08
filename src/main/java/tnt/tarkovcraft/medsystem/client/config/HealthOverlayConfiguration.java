package tnt.tarkovcraft.medsystem.client.config;

import dev.toma.configuration.config.Configurable;
import tnt.tarkovcraft.core.client.config.ConfigurableOverlay;
import tnt.tarkovcraft.core.util.HorizontalAlignment;
import tnt.tarkovcraft.core.util.VerticalAlignment;

public class HealthOverlayConfiguration extends ConfigurableOverlay {

    @Configurable
    @Configurable.DecimalRange(min = 0.5F, max = 2.5F)
    @Configurable.Gui.Slider
    @Configurable.Gui.NumberFormat("0.0#")
    @Configurable.Comment(localize = true, value = "Health overlay scale")
    public float scale = 1.0F;

    @Configurable
    @Configurable.Range(min = 0, max = 255)
    @Configurable.Gui.Slider
    @Configurable.Comment(localize = true, value = "Health overlay transparency")
    public int transparency = 136;

    @Configurable
    @Configurable.Gui.ColorValue
    @Configurable.Comment(localize = true, value = "Overlay color for dead body parts")
    public String deadLimbColor = "#444444";

    @Configurable
    @Configurable.Gui.ColorValue
    @Configurable.Comment(localize = true, value = "Color schema used for color blending of body part overlay based on the part health")
    public String[] colorSchema = { "#00FF00", "#FFFF00", "#FF0000" };

    public HealthOverlayConfiguration(boolean enabled, HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment, int x, int y) {
        super(enabled, horizontalAlignment, verticalAlignment, x, y);
    }
}
