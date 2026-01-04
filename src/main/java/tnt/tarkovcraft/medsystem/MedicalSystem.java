package tnt.tarkovcraft.medsystem;

import dev.toma.configuration.Configuration;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tnt.tarkovcraft.core.api.event.RegisterWeightProvidersEvent;
import tnt.tarkovcraft.medsystem.common.DamageHandler;
import tnt.tarkovcraft.medsystem.common.MedicalSystemEventHandler;
import tnt.tarkovcraft.medsystem.common.TarkovCraftCommand;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffects;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.*;
import tnt.tarkovcraft.medsystem.common.status.BloodSystemEventHandler;
import tnt.tarkovcraft.medsystem.integration.core.BloodContainerWeightProvider;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;

@Mod(MedSystemConstants.MOD_ID)
public final class MedicalSystem {

    public static final Logger LOGGER = LogManager.getLogger("MedicalSystem");

    public static final HealthSystem HEALTH_SYSTEM = new HealthSystem();
    public static final DamageEffects DAMAGE_EFFECTS = new DamageEffects();

    private static MedSystemConfig config;

    public MedicalSystem(IEventBus modEventBus, ModContainer container) {
        config = Configuration.registerSimpleYmlConfig(MedSystemConfig.class);

        modEventBus.addListener(this::createRegistries);
        modEventBus.addListener(this::modifyDefaultComponents);
        modEventBus.addListener(this::registerCustomWeightProviders);
        modEventBus.register(new MedicalSystemNetwork());

        NeoForge.EVENT_BUS.register(new DamageHandler());
        NeoForge.EVENT_BUS.register(new MedicalSystemEventHandler());
        NeoForge.EVENT_BUS.register(new BloodSystemEventHandler());
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
        MedSystemDamageEffectFunctions.REGISTRY.register(modEventBus);
        MedSystemDamageEffectConditions.REGISTRY.register(modEventBus);
        MedSystemDamageEffectEvents.REGISTRY.register(modEventBus);
        MedSystemStateFilters.REGISTRY.register(modEventBus);
        MedSystemParticleTypes.REGISTRY.register(modEventBus);
        MedSystemArgumentTypes.REGISTRY.register(modEventBus);
    }

    public static MedSystemConfig getConfig() {
        return config;
    }

    private void createRegistries(NewRegistryEvent event) {
        event.register(MedSystemRegistries.STATUS_EFFECT);
        event.register(MedSystemRegistries.EFFECT_GROUP_ITEM);
        event.register(MedSystemRegistries.DAMAGE_EFFECT_CONDITION);
        event.register(MedSystemRegistries.DAMAGE_EFFECT_FUNCTION);
        event.register(MedSystemRegistries.DAMAGE_EFFECT_EVENT);
        event.register(MedSystemRegistries.STATE_MATCHER);
        event.register(MedSystemRegistries.STATUS_EFFECT_PREDICATE);
    }

    private void addReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(HealthSystem.IDENTIFIER, HEALTH_SYSTEM);
        event.addListener(DamageEffects.IDENTIFIER, DAMAGE_EFFECTS);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        TarkovCraftCommand.create(event.getDispatcher(), event.getBuildContext());
    }

    private void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        if (config.addHitEffectsToVanillaItems)
            VanillaItemComponentAssignments.adjustItemData((item, attr) -> event.modify(item, builder -> builder.set(MedSystemItemComponents.SIDE_EFFECTS.get(), attr)));
    }

    private void registerCustomWeightProviders(RegisterWeightProvidersEvent event) {
        event.register(BloodContainerWeightProvider.PROVIDER_ID, new BloodContainerWeightProvider());
    }

    public static Identifier createIdentifier(String path) {
        return Identifier.fromNamespaceAndPath(MedSystemConstants.MOD_ID, path);
    }
}
