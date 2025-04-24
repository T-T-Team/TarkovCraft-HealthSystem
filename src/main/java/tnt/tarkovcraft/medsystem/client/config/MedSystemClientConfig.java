package tnt.tarkovcraft.medsystem.client.config;

import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.FieldVisibility;
import tnt.tarkovcraft.medsystem.MedicalSystem;

@Config(id = MedicalSystem.MOD_ID, filename = "medicalsystem-client", group = MedicalSystem.MOD_ID)
public final class MedSystemClientConfig {

    @Configurable
    @Configurable.Gui.Visibility(FieldVisibility.ADVANCED)
    public boolean enableHitboxDebugRenderer = false;
}
