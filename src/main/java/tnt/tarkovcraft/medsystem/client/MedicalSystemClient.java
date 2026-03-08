package tnt.tarkovcraft.medsystem.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.toma.configuration.Configuration;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugEntryNoop;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.particles.ParticleLimit;
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
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;
import tnt.tarkovcraft.core.api.event.client.RegisterOnScreenHintEvent;
import tnt.tarkovcraft.core.api.event.client.RegisterPostShaderProgramsEvent;
import tnt.tarkovcraft.core.client.overlay.StaminaLayer;
import tnt.tarkovcraft.core.client.screen.navigation.CoreNavigators;
import tnt.tarkovcraft.core.client.screen.navigation.NavigationEntry;
import tnt.tarkovcraft.core.client.screen.navigation.OptionalNavigationEntry;
import tnt.tarkovcraft.core.client.shader.DynamicTransformsPipelineModifier;
import tnt.tarkovcraft.core.util.helper.TextHelper;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.client.config.MedSystemClientConfig;
import tnt.tarkovcraft.medsystem.client.debug.HitResultInfoDebugRenderer;
import tnt.tarkovcraft.medsystem.client.model.properties.BloodVolumeItemModelProperty;
import tnt.tarkovcraft.medsystem.client.model.properties.IsEmptyBloodContainerItemModelProperty;
import tnt.tarkovcraft.medsystem.client.overlay.HealthLayer;
import tnt.tarkovcraft.medsystem.client.overlay.UnconsciousLayer;
import tnt.tarkovcraft.medsystem.client.particle.BloodDecalParticle;
import tnt.tarkovcraft.medsystem.client.particle.BloodDripParticle;
import tnt.tarkovcraft.medsystem.client.screen.HealthContainerScreen;
import tnt.tarkovcraft.medsystem.client.screen.HealthScreen;
import tnt.tarkovcraft.medsystem.client.shader.*;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemParticleTypes;
import tnt.tarkovcraft.medsystem.integration.core.GiveUpOnScreenHint;
import tnt.tarkovcraft.medsystem.network.message.C2S_RequestGiveUp;
import tnt.tarkovcraft.medsystem.network.message.C2S_SendMyRotation;

import java.util.UUID;

@Mod(value = MedSystemConstants.MOD_ID, dist = Dist.CLIENT)
public final class MedicalSystemClient {

    public static final KeyMapping.Category KEYMAPPING_CATEGORY = new KeyMapping.Category(MedicalSystem.createIdentifier("keymap"));
    public static final KeyMapping KEY_GIVE_UP = new KeyMapping(
            TextHelper.createKeybindName(MedSystemConstants.MOD_ID, "give_up"),
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_X,
            KEYMAPPING_CATEGORY
    );
    public static final ParticleLimit BLOOD_PARTICLES_LIMIT = new ParticleLimit(2000);
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
        config = Configuration.registerSimpleYmlConfig(MedSystemClientConfig.class);

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::registerGuiLayer);
        modEventBus.addListener(this::registerConditionalItemModelProperties);
        modEventBus.addListener(this::registerRangeSelectItemModelProperties);
        modEventBus.addListener(this::registerKeyBinds);
        modEventBus.addListener(this::registerOnScreenHints);
        modEventBus.addListener(this::registerRenderStateExtensions);
        modEventBus.addListener(this::registerShaderPrograms);
        modEventBus.addListener(this::registerParticleProviders);
        modEventBus.addListener(this::registerDebugRenderers);
        modEventBus.addListener(this::registerDebugEntries);

        NeoForge.EVENT_BUS.addListener(this::onKeyInput);
        NeoForge.EVENT_BUS.addListener(this::prepareLayerRender);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onMouseInput);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, this::onMouseWheelInput);

        CoreNavigators.CHARACTER_NAVIGATION_PROVIDER.register(HEALTH);
    }

    public static MedSystemClientConfig getConfig() {
        return config;
    }

    public static void onHealthContainerUpdated(IAttachmentHolder holder, HealthContainer container) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen screen = minecraft.screen;
        if (screen instanceof HealthContainerScreen healthContainerScreen) {
            healthContainerScreen.onHealthContainerUpdated(holder, container);
        }
    }

    private void setup(FMLClientSetupEvent event) {
        DynamicTransformsPipelineModifier.addTargetPipeline(PainEffectShaderProgram.PIPELINE);
        DynamicTransformsPipelineModifier.addTargetPipeline(ConcussionEffectShaderProgram.PIPELINE);
        DynamicTransformsPipelineModifier.addTargetPipeline(BloodLossEffectShaderProgram.PIPELINE);
        DynamicTransformsPipelineModifier.addTargetPipeline(PainReliefEffectShaderProgram.PIPELINE);
    }

    @SuppressWarnings({"RedundantCast", "unchecked"})
    private void registerRenderStateExtensions(RegisterRenderStateModifiersEvent event) {
        event.registerEntityModifier(
                (Class<? extends EntityRenderer<? extends LivingEntity, ? extends LivingEntityRenderState>>)(Object) LivingEntityRenderer.class,
                (entity, state) -> {
                    EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(entity);
                    if (bloodSystem == null)
                        return;
                    EntityBloodSystemDefinition definition = bloodSystem.getDefinition();
                    state.setRenderData(RenderStateExtensions.SPECIAL_POSE, definition.hasSpecialUnconsciousPoseRenderer());
                    state.setRenderData(RenderStateExtensions.PASSENGER, entity.isPassenger());
                    state.setRenderData(RenderStateExtensions.UNCONSCIOUS, bloodSystem.isUnconscious());
                }
        );
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
            if (BloodSystemManager.canSkipUnconsciousMode(player)) {
                ClientPacketDistributor.sendToServer(new C2S_RequestGiveUp());
            }
        }
    }

    private void registerOnScreenHints(RegisterOnScreenHintEvent event) {
        event.register(new GiveUpOnScreenHint());
    }

    private void registerConditionalItemModelProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(MedicalSystem.createIdentifier("empty_blood_container"), IsEmptyBloodContainerItemModelProperty.CODEC);
    }

    private void registerRangeSelectItemModelProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(MedicalSystem.createIdentifier("blood_volume"), BloodVolumeItemModelProperty.CODEC);
    }

    private void onMouseInput(InputEvent.MouseButton.Pre event) {
        this.cancelInputEventIfUnconscious(event);
    }

    private void onMouseWheelInput(InputEvent.MouseScrollingEvent event) {
        this.cancelInputEventIfUnconscious(event);
    }

    private void registerShaderPrograms(RegisterPostShaderProgramsEvent event) {
        event.registerMany(
                new PainEffectShaderProgram(),
                new ConcussionEffectShaderProgram(),
                new UnconsciousEffectShaderProgram(),
                new BloodLossEffectShaderProgram(),
                new PainReliefEffectShaderProgram()
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
        ClientPacketDistributor.sendToServer(new C2S_SendMyRotation(yBodyRot));
    }

    private void registerDebugRenderers(RegisterDebugRenderersEvent event) {
        Minecraft client = Minecraft.getInstance();
        if (client.debugEntries.isCurrentlyEnabled(HitResultInfoDebugRenderer.IDENTIFIER))
            event.register(HitResultInfoDebugRenderer.INSTANCE);
    }

    private void registerDebugEntries(RegisterDebugEntriesEvent event) {
        event.register(HitResultInfoDebugRenderer.IDENTIFIER, new DebugEntryNoop());
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
