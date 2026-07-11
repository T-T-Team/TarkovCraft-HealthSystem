package tnt.tarkovcraft.medsystem;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.format.ConfigFormats;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tnt.tarkovcraft.core.api.event.RegisterWeightProvidersEvent;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.DamageHandler;
import tnt.tarkovcraft.medsystem.common.MedicalSystemEventHandler;
import tnt.tarkovcraft.medsystem.common.TarkovCraftCommand;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.damage.DamageResolverManager;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventManager;
import tnt.tarkovcraft.medsystem.common.init.*;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteractions;
import tnt.tarkovcraft.medsystem.integration.carryon.CarryOnIntegration;
import tnt.tarkovcraft.medsystem.integration.core.BloodContainerWeightProvider;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;

@Mod(MedSystemConstants.MOD_ID)
public final class MedicalSystem {

    public static final Logger LOGGER = LogManager.getLogger("MedicalSystem");

    public static final DamageResolverManager DAMAGE_RESOLVER = new DamageResolverManager();
    public static final HealthSystem HEALTH_SYSTEM = new HealthSystem();
    public static final HealthEventManager HEALTH_EVENT = new HealthEventManager();
    public static final BloodSystemManager BLOOD_SYSTEM = new BloodSystemManager();

    private static MedSystemConfig config;

    public MedicalSystem(IEventBus modEventBus, ModContainer container) {
        config = Configuration.registerConfig(MedSystemConfig.class, ConfigFormats.YAML).getConfigInstance();

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::register);
        modEventBus.addListener(this::createRegistries);
        modEventBus.addListener(this::registerCustomWeightProviders);
        modEventBus.register(new MedicalSystemNetwork());

        NeoForge.EVENT_BUS.register(new DamageHandler());
        NeoForge.EVENT_BUS.register(new MedicalSystemEventHandler());
        NeoForge.EVENT_BUS.addListener(this::addReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);

        EntityInteractions.init();

        MedSystemAttributes.REGISTRY.register(modEventBus);
        MedSystemItems.REGISTRY.register(modEventBus);
        MedSystemDataAttachments.REGISTRY.register(modEventBus);
        MedSystemItemComponents.REGISTRY.register(modEventBus);
        MedSystemStats.REGISTRY.register(modEventBus);
        MedSystemSkillEvents.REGISTRY.register(modEventBus);
        MedSystemStatusEffects.REGISTRY.register(modEventBus);
        MedSystemCreativeTabs.REGISTRY.register(modEventBus);
        MedSystemHealthEventSources.REGISTRY.register(modEventBus);
        MedSystemParticleTypes.REGISTRY.register(modEventBus);
        MedSystemArgumentTypes.REGISTRY.register(modEventBus);
        MedSystemConsumeEffects.REGISTRY.register(modEventBus);
        MedSystemEntityPoses.REGISTRY.register(modEventBus);
        MedSystemCriterionTriggers.REGISTRY.register(modEventBus);
    }

    public static MedSystemConfig getConfig() {
        return config;
    }

    private void register(RegisterEvent event) {
        event.register(MedSystemRegistries.Keys.EFFECT_GROUP_ITEM, MedSystemRegistries::registerEffectGroupItems);
        event.register(MedSystemRegistries.Keys.STATE_MATCHER, MedSystemRegistries::registerStateMatchers);
        event.register(MedSystemRegistries.Keys.EFFECT_RECOVERY_APPLICATOR, MedSystemRegistries::registerEffectRecoveryApplicators);
        event.register(MedSystemRegistries.Keys.DAMAGE_CONDITIONS, MedSystemRegistries::registerDamageConditions);
        event.register(MedSystemRegistries.Keys.DAMAGE_FUNCTIONS, MedSystemRegistries::registerDamageFunctions);
        event.register(MedSystemRegistries.Keys.HEALTH_EVENT_CONDITION, MedSystemRegistries::registerHealthEventConditions);
        event.register(MedSystemRegistries.Keys.HEALTH_EVENT_ACTION, MedSystemRegistries::registerHealthEventActions);
        event.register(MedSystemRegistries.Keys.HEALTH_EVENT_FUNCTION, MedSystemRegistries::registerHealthEventFunctions);
        event.register(MedSystemRegistries.Keys.BLOOD_LEVEL_EFFECT, MedSystemRegistries::registerBloodLevelEffects);
    }

    private void setup(FMLCommonSetupEvent event) {
        LOGGER.info("Checking loaded mods for compatibility...");
        ModList modList = ModList.get();
        if (modList.isLoaded("carryon")) {
            LOGGER.info("'Carry On' mod detected, enabling integration");
            CarryOnIntegration.initCommon();
        }
    }

    private void createRegistries(NewRegistryEvent event) {
        event.register(MedSystemRegistries.STATUS_EFFECT);
        event.register(MedSystemRegistries.EFFECT_GROUP_ITEM);
        event.register(MedSystemRegistries.STATE_MATCHER);
        event.register(MedSystemRegistries.EFFECT_RECOVERY_APPLICATOR);
        event.register(MedSystemRegistries.DAMAGE_CONDITIONS);
        event.register(MedSystemRegistries.DAMAGE_FUNCTIONS);
        event.register(MedSystemRegistries.HEALTH_EVENT_TRIGGER_SOURCE);
        event.register(MedSystemRegistries.HEALTH_EVENT_FUNCTION);
        event.register(MedSystemRegistries.HEALTH_EVENT_CONDITION);
        event.register(MedSystemRegistries.HEALTH_EVENT_ACTION);
        event.register(MedSystemRegistries.BLOOD_LEVEL_EFFECT);
        event.register(MedSystemRegistries.CONSUME_EFFECT);
    }

    private void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(DAMAGE_RESOLVER);
        event.addListener(HEALTH_SYSTEM);
        event.addListener(HEALTH_EVENT);
        BLOOD_SYSTEM.registerServerDataListeners(event);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        TarkovCraftCommand.create(event.getDispatcher(), event.getBuildContext());
    }

    private void registerCustomWeightProviders(RegisterWeightProvidersEvent event) {
        event.register(BloodContainerWeightProvider.PROVIDER_ID, new BloodContainerWeightProvider());
    }

    public static ResourceLocation createIdentifier(String path) {
        return ResourceLocation.fromNamespaceAndPath(MedSystemConstants.MOD_ID, path);
    }
}
