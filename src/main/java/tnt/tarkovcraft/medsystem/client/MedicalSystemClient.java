package tnt.tarkovcraft.medsystem.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.format.ConfigFormats;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import tnt.tarkovcraft.core.api.event.client.RegisterOnScreenHintEvent;
import tnt.tarkovcraft.core.api.event.client.RegisterPostShaderProgramsEvent;
import tnt.tarkovcraft.core.client.TarkovCraftCoreClient;
import tnt.tarkovcraft.core.client.overlay.StaminaLayer;
import tnt.tarkovcraft.core.client.screen.navigation.CoreNavigators;
import tnt.tarkovcraft.core.client.screen.navigation.NavigationEntry;
import tnt.tarkovcraft.core.client.screen.navigation.OptionalNavigationEntry;
import tnt.tarkovcraft.core.util.helper.TextHelper;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.client.config.MedSystemClientConfig;
import tnt.tarkovcraft.medsystem.client.model.tint.BloodTintSource;
import tnt.tarkovcraft.medsystem.client.overlay.HealthLayer;
import tnt.tarkovcraft.medsystem.client.overlay.UnconsciousLayer;
import tnt.tarkovcraft.medsystem.client.particle.BloodDecalParticle;
import tnt.tarkovcraft.medsystem.client.particle.BloodDripParticle;
import tnt.tarkovcraft.medsystem.client.screen.HealthScreen;
import tnt.tarkovcraft.medsystem.client.shader.*;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodContainer;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousAnimationState;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItems;
import tnt.tarkovcraft.medsystem.common.init.MedSystemParticleTypes;
import tnt.tarkovcraft.medsystem.integration.core.GiveUpOnScreenHint;
import tnt.tarkovcraft.medsystem.network.message.C2S_RequestGiveUp;
import tnt.tarkovcraft.medsystem.network.message.C2S_SendMyRotation;

import java.util.UUID;

@Mod(value = MedSystemConstants.MOD_ID, dist = Dist.CLIENT)
public final class MedicalSystemClient {

    public static final KeyMapping KEY_GIVE_UP = new KeyMapping(
            TextHelper.createKeybindName(MedSystemConstants.MOD_ID, "give_up"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_X,
            TarkovCraftCoreClient.SHARED_CATEGORY
    );
    public static final KeyMapping KEY_OPEN_HEALTH = new KeyMapping(
            TextHelper.createKeybindName(MedSystemConstants.MOD_ID, "open_health"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.UNKNOWN.getValue(),
            TarkovCraftCoreClient.SHARED_CATEGORY
    );
    private static MedSystemClientConfig config;

    public static final NavigationEntry HEALTH = new OptionalNavigationEntry(
            TextHelper.createScreenTitle(MedSystemConstants.MOD_ID, "health"),
            (parent, userId) -> {
                UUID clientId = Minecraft.getInstance().player.getUUID();
                return userId.equals(clientId);
            },
            HealthScreen::new,
            25
    );

    public MedicalSystemClient(IEventBus modEventBus, ModContainer container) {
        config = Configuration.registerConfig(MedSystemClientConfig.class, ConfigFormats.YAML).getConfigInstance();

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::registerGuiLayer);
        modEventBus.addListener(this::registerItemModelTintSources);
        modEventBus.addListener(this::registerKeyBinds);
        modEventBus.addListener(this::registerOnScreenHints);
        modEventBus.addListener(this::registerShaderPrograms);
        modEventBus.addListener(this::registerParticleProviders);

        NeoForge.EVENT_BUS.addListener(this::onKeyInput);
        NeoForge.EVENT_BUS.addListener(this::prepareLayerRender);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::displayScreen);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onMouseInput);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onMouseWheelInput);

        CoreNavigators.CHARACTER_NAVIGATION_PROVIDER.register(HEALTH);
    }

    public static MedSystemClientConfig getConfig() {
        return config;
    }

    private void setup(FMLClientSetupEvent event) {
        ItemProperties.register(MedSystemItems.BLOODBAG.asItem(), MedicalSystem.createIdentifier("blood_volume"), (itemStack, level, entity, seed) -> {
            BloodContainer container = itemStack.get(MedSystemItemComponents.BLOOD_CONTAINER);
            if (container == null) {
                return 0.0F;
            }
            return container.value() / container.capacity();
        });
    }

    private void registerGuiLayer(RegisterGuiLayersEvent event) {
        event.registerAbove(StaminaLayer.LAYER_ID, HealthLayer.LAYER_ID, new HealthLayer());
        event.registerBelowAll(UnconsciousLayer.LAYER_ID, new UnconsciousLayer());
    }

    private void prepareLayerRender(RenderGuiLayerEvent.Pre event) {
        if (!config.renderHealth && event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
            event.setCanceled(true);
        }
    }

    private void registerKeyBinds(RegisterKeyMappingsEvent event) {
        event.register(KEY_GIVE_UP);
        event.register(KEY_OPEN_HEALTH);
    }

    private void onKeyInput(InputEvent.Key event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (KEY_GIVE_UP.consumeClick()) {
            if (BloodSystemManager.canSkipUnconsciousMode(player)) {
                PacketDistributor.sendToServer(new C2S_RequestGiveUp());
            }
        }
        if (KEY_OPEN_HEALTH.consumeClick()) {
            if (HealthSystem.hasCustomHealth(player)) {
                minecraft.setScreen(new HealthScreen(null, player.getUUID()));
            }
        }
    }

    private void registerOnScreenHints(RegisterOnScreenHintEvent event) {
        event.register(new GiveUpOnScreenHint());
    }

    private void registerItemModelTintSources(RegisterColorHandlersEvent.Item event) {
        event.register(BloodTintSource.INSTANCE, MedSystemItems.BLOODBAG);
    }

    private void onMouseInput(InputEvent.MouseButton.Pre event) {
        this.cancelInputEventIfUnconscious(event);
    }

    private void onMouseWheelInput(InputEvent.MouseScrollingEvent event) {
        this.cancelInputEventIfUnconscious(event);
    }

    private void registerShaderPrograms(RegisterPostShaderProgramsEvent event) {
        event.registerMany(
                PainEffectShaderProgram.INSTANCE,
                ConcussionEffectShaderProgram.INSTANCE,
                BloodLossEffectShaderProgram.INSTANCE,
                PainReliefEffectShaderProgram.INSTANCE,
                UnconsciousEffectShaderProgram.INSTANCE
        );
    }

    private void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MedSystemParticleTypes.BLOOD_DRIP.get(), BloodDripParticle.Provider::new);
        event.registerSpriteSet(MedSystemParticleTypes.BLOOD_DECAL.get(), BloodDecalParticle.Provider::new);
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || !MedicalSystem.getConfig().forceEntityRotationSynchronization) return;
        float yBodyRot = Mth.wrapDegrees(client.player.yBodyRot);
        PacketDistributor.sendToServer(new C2S_SendMyRotation(yBodyRot));
    }

    private void displayScreen(ScreenEvent.Opening event) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player != null && BloodSystemManager.isUnconscious(player) && event.getScreen() instanceof InventoryScreen) {
            event.setCanceled(true);
        }
    }

    private <E extends ICancellableEvent> void cancelInputEventIfUnconscious(E event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null)
            return;
        Screen screen = minecraft.screen;
        if (screen != null)
            return; // allows screen events
        if (BloodSystemManager.isUnconscious(player)) {
            event.setCanceled(true);
        }
    }
}
