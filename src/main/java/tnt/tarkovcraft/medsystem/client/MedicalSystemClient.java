package tnt.tarkovcraft.medsystem.client;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.format.ConfigFormats;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.client.config.MedSystemClientConfig;

@Mod(value = MedicalSystem.MOD_ID, dist = Dist.CLIENT)
public final class MedicalSystemClient {

    private static MedSystemClientConfig config;

    public MedicalSystemClient(IEventBus modEventBus, ModContainer container) {
        config = Configuration.registerConfig(MedSystemClientConfig.class, ConfigFormats.YAML).getConfigInstance();
    }

    public static MedSystemClientConfig getConfig() {
        return config;
    }
}
