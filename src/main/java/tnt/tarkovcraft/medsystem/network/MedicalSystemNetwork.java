package tnt.tarkovcraft.medsystem.network;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.Identifier;
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

    public static Identifier createId(Class<? extends CustomPacketPayload> type) {
        String name = type.getSimpleName().toLowerCase(Locale.ROOT);
        return MedicalSystem.createIdentifier("net/" + name);
    }

    @SubscribeEvent
    private void onRegistration(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registry = event.registrar(NETWORK_ID).executesOn(HandlerThread.MAIN);

        registry.playToClient(S2C_OpenBodyPartSelectScreen.TYPE, S2C_OpenBodyPartSelectScreen.CODEC, S2C_OpenBodyPartSelectScreen::handleMessage);
        registry.playToClient(S2C_RefreshEntityDimensions.TYPE, S2C_RefreshEntityDimensions.CODEC, S2C_RefreshEntityDimensions::handleMessage);

        registry.playToServer(C2S_SelectBodyPart.TYPE, C2S_SelectBodyPart.CODEC, C2S_SelectBodyPart::handleMessage);
        registry.playToServer(C2S_RequestGiveUp.TYPE, C2S_RequestGiveUp.CODEC, C2S_RequestGiveUp::handleMessage);

        registry.configurationToClient(S2C_SendHealthDefinitions.TYPE, S2C_SendHealthDefinitions.CODEC, S2C_SendHealthDefinitions::handleMessage);
    }

    @SubscribeEvent
    private void registerConfigurationTasks(RegisterConfigurationTasksEvent event) {
        event.register(new HealthContainerSynchronizationTask(event.getListener()));
    }

    private record HealthContainerSynchronizationTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {

        public static final Type TYPE = new Type(MedicalSystem.createIdentifier("health_container_sync"));

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
