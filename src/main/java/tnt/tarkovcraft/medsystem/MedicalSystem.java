package tnt.tarkovcraft.medsystem;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.format.ConfigFormats;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tnt.tarkovcraft.core.api.event.RegisterWeightProvidersEvent;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.DamageHandler;
import tnt.tarkovcraft.medsystem.common.MedicalSystemEventHandler;
import tnt.tarkovcraft.medsystem.common.TarkovCraftCommand;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventManager;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.*;
import tnt.tarkovcraft.medsystem.integration.core.BloodContainerWeightProvider;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;

@Mod(MedSystemConstants.MOD_ID)
public final class MedicalSystem {

    public static final Logger LOGGER = LogManager.getLogger("MedicalSystem");

    public static final HealthSystem HEALTH_SYSTEM = new HealthSystem();
    public static final HealthEventManager STATUS_EFFECT_EVENTS = new HealthEventManager();
    public static final BloodSystemManager BLOOD_SYSTEM = new BloodSystemManager();

    private static MedSystemConfig config;

    public MedicalSystem(IEventBus modEventBus, ModContainer container) {
        config = Configuration.registerConfig(MedSystemConfig.class, ConfigFormats.YAML).getConfigInstance();

        modEventBus.addListener(this::createRegistries);
        modEventBus.addListener(this::registerCustomWeightProviders);
        modEventBus.register(new MedicalSystemNetwork());

        NeoForge.EVENT_BUS.register(new DamageHandler());
        NeoForge.EVENT_BUS.register(new MedicalSystemEventHandler());
        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        MedSystemAttributes.REGISTRY.register(modEventBus);
        MedSystemItems.REGISTRY.register(modEventBus);
        MedSystemDataAttachments.REGISTRY.register(modEventBus);
        MedSystemItemComponents.REGISTRY.register(modEventBus);
        MedSystemStats.REGISTRY.register(modEventBus);
        MedSystemSkillEvents.REGISTRY.register(modEventBus);
        MedSystemStatusEffects.REGISTRY.register(modEventBus);
        MedSystemStatusEffectPredicates.REGISTRY.register(modEventBus);
        MedSystemCreativeTabs.REGISTRY.register(modEventBus);
        MedSystemStatusEffectGroupItems.REGISTRY.register(modEventBus);
        MedSystemHealthEventSources.REGISTRY.register(modEventBus);
        MedSystemHealthEventFunctions.REGISTRY.register(modEventBus);
        MedSystemHealthEventConditions.REGISTRY.register(modEventBus);
        MedSystemHealthEventActions.REGISTRY.register(modEventBus);
        MedSystemStateFilters.REGISTRY.register(modEventBus);
        MedSystemParticleTypes.REGISTRY.register(modEventBus);
        MedSystemArgumentTypes.REGISTRY.register(modEventBus);
        MedSystemBloodLevelEffects.REGISTRY.register(modEventBus);
        MedSystemEntityPoses.REGISTRY.register(modEventBus);
    }

    public static MedSystemConfig getConfig() {
        return config;
    }

    private void createRegistries(NewRegistryEvent event) {
        event.register(MedSystemRegistries.STATUS_EFFECT);
        event.register(MedSystemRegistries.EFFECT_GROUP_ITEM);
        event.register(MedSystemRegistries.STATE_MATCHER);
        event.register(MedSystemRegistries.STATUS_EFFECT_PREDICATE);
        event.register(MedSystemRegistries.HEALTH_EVENT_TRIGGER_SOURCE);
        event.register(MedSystemRegistries.HEALTH_EVENT_FUNCTION);
        event.register(MedSystemRegistries.HEALTH_EVENT_CONDITION);
        event.register(MedSystemRegistries.HEALTH_EVENT_ACTION);
        event.register(MedSystemRegistries.BLOOD_LEVEL_EFFECT);
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(HEALTH_SYSTEM);
        event.addListener(STATUS_EFFECT_EVENTS);
        BLOOD_SYSTEM.registerServerDataListeners(event);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        TarkovCraftCommand.create(event.getDispatcher(), event.getBuildContext());
    }

    private void registerCustomWeightProviders(RegisterWeightProvidersEvent event) {
        event.register(BloodContainerWeightProvider.PROVIDER_ID, new BloodContainerWeightProvider());
    }

    public static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MedSystemConstants.MOD_ID, path);
    }
}
