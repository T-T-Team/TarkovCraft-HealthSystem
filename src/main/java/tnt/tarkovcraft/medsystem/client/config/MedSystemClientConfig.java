package tnt.tarkovcraft.medsystem.client.config;

import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.FieldVisibility;
import tnt.tarkovcraft.core.util.HorizontalAlignment;
import tnt.tarkovcraft.core.util.VerticalAlignment;
import tnt.tarkovcraft.medsystem.MedicalSystem;

@Config(id = MedicalSystem.MOD_ID + "-client", filename = "medicalsystem-client", group = MedicalSystem.MOD_ID)
public final class MedSystemClientConfig {

    @Configurable
    @Configurable.Gui.Visibility(FieldVisibility.ADVANCED)
    public boolean enableHitboxDebugRenderer = false;

    @Configurable
    public HealthOverlayConfiguration healthOverlay = new HealthOverlayConfiguration(true, HorizontalAlignment.LEFT, VerticalAlignment.TOP, 0, 0);
}
