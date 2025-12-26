package tnt.tarkovcraft.medsystem.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.format.ConfigFormats;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;
import tnt.tarkovcraft.core.api.event.client.RegisterOnScreenHintEvent;
import tnt.tarkovcraft.core.client.overlay.StaminaLayer;
import tnt.tarkovcraft.core.client.screen.navigation.CoreNavigators;
import tnt.tarkovcraft.core.client.screen.navigation.NavigationEntry;
import tnt.tarkovcraft.core.client.screen.navigation.OptionalNavigationEntry;
import tnt.tarkovcraft.core.util.helper.TextHelper;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.client.config.MedSystemClientConfig;
import tnt.tarkovcraft.medsystem.client.overlay.HealthLayer;
import tnt.tarkovcraft.medsystem.client.overlay.UnconsciousLayer;
import tnt.tarkovcraft.medsystem.client.screen.HealthContainerScreen;
import tnt.tarkovcraft.medsystem.client.screen.HealthScreen;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItems;
import tnt.tarkovcraft.medsystem.common.status.BloodContainer;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;
import tnt.tarkovcraft.medsystem.integration.core.GiveUpOnScreenHint;
import tnt.tarkovcraft.medsystem.network.message.C2S_RequestGiveUp;

import java.util.UUID;

@Mod(value = MedicalSystem.MOD_ID, dist = Dist.CLIENT)
public final class MedicalSystemClient {

    public static final String KEYMAPPING_CATEGORY = "key.category.medsystem.keymap";
    public static final KeyMapping KEY_GIVE_UP = new KeyMapping(
            TextHelper.createKeybindName(MedicalSystem.MOD_ID, "give_up"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            KEYMAPPING_CATEGORY
    );
    private static MedSystemClientConfig config;

    public static final NavigationEntry HEALTH = new OptionalNavigationEntry(
            TextHelper.createScreenTitle(MedicalSystem.MOD_ID, "health"),
            (parent, userId) -> {
                UUID clientId = Minecraft.getInstance().player.getUUID();
                return userId.equals(clientId);
            },
            HealthScreen::new,
            25
    );

    public MedicalSystemClient(IEventBus modEventBus, ModContainer container) {
        config = Configuration.registerConfig(MedSystemClientConfig.class, ConfigFormats.YAML).getConfigInstance();

        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(this::registerGuiLayer);
        modEventBus.addListener(this::registerKeyBinds);
        modEventBus.addListener(this::registerOnScreenHints);

        NeoForge.EVENT_BUS.addListener(this::onKeyInput);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onMouseInput);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onMouseWheelInput);
        NeoForge.EVENT_BUS.addListener(this::prepareLayerRender);

        CoreNavigators.CHARACTER_NAVIGATION_PROVIDER.register(HEALTH);
    }

    public static MedSystemClientConfig getConfig() {
        return config;
    }

    public static void notifyHealthContainerUpdate(IAttachmentHolder holder, HealthContainer container) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = minecraft.screen;
        if (screen instanceof HealthContainerScreen healthContainerScreen) {
            healthContainerScreen.onHealthContainerUpdated(holder, container);
        }
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        ItemProperties.register(MedSystemItems.BLOODBAG.asItem(), MedicalSystem.resource("blood_volume"), (itemStack, level, entity, seed) -> {
            BloodContainer container = itemStack.get(MedSystemItemComponents.BLOOD_CONTAINER);
            if (container == null) {
                return 0.0F;
            }
            return container.value() / container.capacity();
        });
    }

    private void registerGuiLayer(RegisterGuiLayersEvent event) {
        event.registerAbove(StaminaLayer.LAYER_ID, HealthLayer.LAYER_ID, new HealthLayer());
        event.registerAbove(HealthLayer.LAYER_ID, UnconsciousLayer.LAYER_ID, new UnconsciousLayer());
    }

    private void prepareLayerRender(RenderGuiLayerEvent.Pre event) {
        if (!config.renderHealth && event.getName().equals(VanillaGuiLayers.PLAYER_HEALTH)) {
            event.setCanceled(true);
        }
    }

    private void registerKeyBinds(RegisterKeyMappingsEvent event) {
        event.register(KEY_GIVE_UP);
    }

    private void onKeyInput(InputEvent.Key event) {
        if (KEY_GIVE_UP.consumeClick()) {
            Minecraft minecraft = Minecraft.getInstance();
            Player player = minecraft.player;
            if (BloodSystem.canGiveUp(player)) {
                PacketDistributor.sendToServer(new C2S_RequestGiveUp());
            }
        }
    }

    private void registerOnScreenHints(RegisterOnScreenHintEvent event) {
        event.register(new GiveUpOnScreenHint());
    }

    private void onMouseInput(InputEvent.MouseButton.Pre event) {
        this.cancelInputEventIfUnconscious(event);
    }

    private void onMouseWheelInput(InputEvent.MouseScrollingEvent event) {
        this.cancelInputEventIfUnconscious(event);
    }

    private <E extends ICancellableEvent> void cancelInputEventIfUnconscious(E event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null)
            return;
        Screen screen = minecraft.screen;
        if (screen != null)
            return; // allows screen events
        if (BloodSystem.isEntityUnconscious(player)) {
            event.setCanceled(true);
        }
    }
}
