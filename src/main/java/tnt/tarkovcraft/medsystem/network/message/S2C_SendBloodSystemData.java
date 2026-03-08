package tnt.tarkovcraft.medsystem.network.message;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;

public record S2C_SendBloodSystemData(BloodSystemManager.NetworkData networkData) implements CustomPacketPayload {

    public static final Identifier PACKET_ID = MedicalSystemNetwork.createId(S2C_SendBloodSystemData.class);
    public static final Type<S2C_SendBloodSystemData> TYPE = new Type<>(PACKET_ID);
    public static final StreamCodec<FriendlyByteBuf, S2C_SendBloodSystemData> CODEC = StreamCodec.composite(
            BloodSystemManager.NetworkData.STREAM_CODEC, S2C_SendBloodSystemData::networkData,
            S2C_SendBloodSystemData::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleMessage(IPayloadContext ctx) {
        MedicalSystem.BLOOD_SYSTEM.receiveServerData(this.networkData);
    }
}
