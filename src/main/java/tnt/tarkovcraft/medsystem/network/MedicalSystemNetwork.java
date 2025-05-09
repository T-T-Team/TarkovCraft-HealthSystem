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
import tnt.tarkovcraft.medsystem.network.message.C2S_SelectBodyPart;
import tnt.tarkovcraft.medsystem.network.message.S2C_OpenBodyPartSelectScreen;
import tnt.tarkovcraft.medsystem.network.message.S2C_SendHealthDefinitions;

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

        registry.playToClient(S2C_OpenBodyPartSelectScreen.TYPE, S2C_OpenBodyPartSelectScreen.CODEC, S2C_OpenBodyPartSelectScreen::handleMessage);

        registry.playToServer(C2S_SelectBodyPart.TYPE, C2S_SelectBodyPart.CODEC, C2S_SelectBodyPart::handleMessage);

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
