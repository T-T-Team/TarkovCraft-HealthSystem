package tnt.tarkovcraft.medsystem.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.network.config.BloodSystemSynchronizationTask;
import tnt.tarkovcraft.medsystem.network.config.HealthContainerSynchronizationTask;
import tnt.tarkovcraft.medsystem.network.message.*;

import java.util.Locale;

public final class MedicalSystemNetwork {

    public static final int VERSION = 1;
    public static final String NETWORK_ID = "MedicalSystemNetwork@" + VERSION;

    public static ResourceLocation createId(Class<? extends CustomPacketPayload> type) {
        String name = type.getSimpleName().toLowerCase(Locale.ROOT);
        return MedicalSystem.createIdentifier("net/" + name);
    }

    @SubscribeEvent
    private void onRegistration(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registry = event.registrar(NETWORK_ID).executesOn(HandlerThread.MAIN);

        registry.playToClient(S2C_OpenLimbSelectScreen.TYPE, S2C_OpenLimbSelectScreen.CODEC, S2C_OpenLimbSelectScreen::handleMessage);
        registry.playToClient(S2C_RefreshEntityDimensions.TYPE, S2C_RefreshEntityDimensions.CODEC, S2C_RefreshEntityDimensions::handleMessage);
        registry.playToClient(S2C_SendEntityRotation.TYPE, S2C_SendEntityRotation.CODEC, S2C_SendEntityRotation::handleMessage);

        registry.playToServer(C2S_SelectLimb.TYPE, C2S_SelectLimb.CODEC, C2S_SelectLimb::handleMessage);
        registry.playToServer(C2S_RequestGiveUp.TYPE, C2S_RequestGiveUp.CODEC, C2S_RequestGiveUp::handleMessage);
        registry.playToServer(C2S_SendMyRotation.TYPE, C2S_SendMyRotation.CODEC, C2S_SendMyRotation::handleMessage);
        registry.playToServer(C2S_RescueDownedEntity.TYPE, C2S_RescueDownedEntity.CODEC, C2S_RescueDownedEntity::handleMessage);

        registry.configurationToClient(S2C_SendHealthDefinitions.TYPE, S2C_SendHealthDefinitions.CODEC, S2C_SendHealthDefinitions::handleMessage);
        registry.configurationToClient(S2C_SendBloodSystemData.TYPE, S2C_SendBloodSystemData.CODEC, S2C_SendBloodSystemData::handleMessage);
    }

    @SubscribeEvent
    private void registerConfigurationTasks(RegisterConfigurationTasksEvent event) {
        event.register(new HealthContainerSynchronizationTask(event.getListener()));
        event.register(new BloodSystemSynchronizationTask(event.getListener()));
    }
}
