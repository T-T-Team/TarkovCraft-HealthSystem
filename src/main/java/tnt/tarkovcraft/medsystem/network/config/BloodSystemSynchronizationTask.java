package tnt.tarkovcraft.medsystem.network.config;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.network.message.S2C_SendBloodSystemData;

import java.util.function.Consumer;

public record BloodSystemSynchronizationTask(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {

    public static final Type TYPE = new Type(MedicalSystem.resource("blood_system_sync"));

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        sender.accept(new S2C_SendBloodSystemData(MedicalSystem.BLOOD_SYSTEM.prepareSynchronizationData()));
        this.listener.finishCurrentTask(TYPE);
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
