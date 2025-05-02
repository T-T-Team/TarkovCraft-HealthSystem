package tnt.tarkovcraft.medsystem;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.ConfigHolder;
import dev.toma.configuration.config.format.ConfigFormats;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import tnt.tarkovcraft.medsystem.common.MedicalSystemEventHandler;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.health.DefaultArmorComponent;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.*;

@Mod(MedicalSystem.MOD_ID)
public final class MedicalSystem {

    public static final String MOD_ID = "medsystem";
    public static final Logger LOGGER = LogManager.getLogger("TarkovCraftMedicalSystem");
    public static final Marker MARKER = MarkerManager.getMarker("MedicalSystem");

    public static final HealthSystem HEALTH_SYSTEM = new HealthSystem();

    private static MedSystemConfig config;

    public MedicalSystem(IEventBus modEventBus, ModContainer container) {
        ConfigHolder<MedSystemConfig> holder = Configuration.registerConfig(MedSystemConfig.class, ConfigFormats.YAML);
        this.addCustomConfigValidations(holder);
        config = holder.getConfigInstance();


        modEventBus.addListener(this::createRegistries);

        NeoForge.EVENT_BUS.register(new MedicalSystemEventHandler());
        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);

        MedSystemDataAttachments.REGISTRY.register(modEventBus);
        MedSystemTransformConditions.REGISTRY.register(modEventBus);
        MedSystemHitboxTransforms.REGISTRY.register(modEventBus);
        MedSystemItemComponents.REGISTRY.register(modEventBus);
        MedSystemStats.REGISTRY.register(modEventBus);
        MedSystemSkillEvents.REGISTRY.register(modEventBus);
        MedSystemAttributes.REGISTRY.register(modEventBus);
    }

    public static MedSystemConfig getConfig() {
        return config;
    }

    private void createRegistries(NewRegistryEvent event) {
        event.register(MedSystemRegistries.TRANSFORM_CONDITION);
        event.register(MedSystemRegistries.TRANSFORM);
    }

    private void addReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(HealthSystem.IDENTIFIER, HEALTH_SYSTEM);
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private void addCustomConfigValidations(ConfigHolder<MedSystemConfig> holder) {
        holder.getConfigValue("simpleArmorCalculation", Boolean.class).ifPresent(value -> value.addValidator(DefaultArmorComponent::checkInUse));
    }
}
