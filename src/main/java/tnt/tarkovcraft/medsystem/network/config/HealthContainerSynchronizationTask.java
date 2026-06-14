package tnt.tarkovcraft.medsystem.network.config;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import tnt.tarkovcraft.medsystem.MedicalSystem;

import java.util.function.Consumer;

public record HealthContainerSynchronizationTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {

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
