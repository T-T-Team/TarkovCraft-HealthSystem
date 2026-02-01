package tnt.tarkovcraft.medsystem.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.network.message.*;

import java.util.Locale;
import java.util.function.Consumer;

public final class MedicalSystemNetwork {

    public static final int VERSION = 1;
    public static final String NETWORK_ID = "MedicalSystemNetwork@" + VERSION;

    public static ResourceLocation createId(Class<? extends CustomPacketPayload> type) {
        String name = type.getSimpleName().toLowerCase(Locale.ROOT);
        return MedicalSystem.resource("net/" + name);
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

        registry.configurationToClient(S2C_SendHealthDefinitions.TYPE, S2C_SendHealthDefinitions.CODEC, S2C_SendHealthDefinitions::handleMessage);
    }

    @SubscribeEvent
    private void registerConfigurationTasks(RegisterConfigurationTasksEvent event) {
        event.register(new HealthContainerSynchronizationTask(event.getListener()));
    }

    private record HealthContainerSynchronizationTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {

        public static final Type TYPE = new Type(MedicalSystem.resource("health_container_sync"));

        @Override
        public void run(Consumer<CustomPacketPayload> sender) {
            sender.accept(MedicalSystem.HEALTH_SYSTEM.getConfigurationPayload());
            this.listener.finishCurrentTask(TYPE);
        }

        @Override
        public Type type() {
            return TYPE;
        }
    }
}
