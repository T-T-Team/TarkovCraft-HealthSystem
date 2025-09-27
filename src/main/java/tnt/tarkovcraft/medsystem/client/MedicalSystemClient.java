package tnt.tarkovcraft.medsystem.client;

import dev.toma.configuration.Configuration;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import tnt.tarkovcraft.core.client.overlay.StaminaLayer;
import tnt.tarkovcraft.core.client.screen.navigation.CoreNavigators;
import tnt.tarkovcraft.core.client.screen.navigation.NavigationEntry;
import tnt.tarkovcraft.core.client.screen.navigation.OptionalNavigationEntry;
import tnt.tarkovcraft.core.util.helper.TextHelper;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.client.config.MedSystemClientConfig;
import tnt.tarkovcraft.medsystem.client.model.properties.BloodVolumeItemModelProperty;
import tnt.tarkovcraft.medsystem.client.model.properties.IsEmptyBloodContainerItemModelProperty;
import tnt.tarkovcraft.medsystem.client.overlay.HealthLayer;
import tnt.tarkovcraft.medsystem.client.overlay.UnconsciousLayer;
import tnt.tarkovcraft.medsystem.client.screen.HealthScreen;

import java.util.UUID;

@Mod(value = MedicalSystem.MOD_ID, dist = Dist.CLIENT)
public final class MedicalSystemClient {

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
        config = Configuration.registerSimpleYmlConfig(MedSystemClientConfig.class);

        modEventBus.addListener(this::registerGuiLayer);
        modEventBus.addListener(this::registerConditionalItemModelProperties);
        modEventBus.addListener(this::registerRangeSelectItemModelProperties);

        NeoForge.EVENT_BUS.addListener(this::prepareLayerRender);
        NeoForge.EVENT_BUS.addListener(ShaderHelper::updateActiveEffects);

        CoreNavigators.CHARACTER_NAVIGATION_PROVIDER.register(HEALTH);
    }

    public static MedSystemClientConfig getConfig() {
        return config;
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

    private void registerConditionalItemModelProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(MedicalSystem.resource("empty_blood_container"), IsEmptyBloodContainerItemModelProperty.CODEC);
    }

    private void registerRangeSelectItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(MedicalSystem.resource("blood_volume"), BloodVolumeItemModelProperty.CODEC);
    }
}
