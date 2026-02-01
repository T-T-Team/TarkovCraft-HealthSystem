package tnt.tarkovcraft.medsystem.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;

public record C2S_SendMyRotation(float yBodyRot) implements CustomPacketPayload {

    public static final ResourceLocation PACKET_ID = MedicalSystemNetwork.createId(C2S_SendMyRotation.class);
    public static final Type<C2S_SendMyRotation> TYPE = new Type<>(PACKET_ID);
    public static final StreamCodec<ByteBuf, C2S_SendMyRotation> CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, C2S_SendMyRotation::yBodyRot,
            C2S_SendMyRotation::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleMessage(IPayloadContext context) {
        Player player = context.player();
        MedSystemConfig config = MedicalSystem.getConfig();
        if (!config.forceEntityRotationSynchronization)
            return;
        player.setYBodyRot(this.yBodyRot);
        PacketDistributor.sendToPlayersTrackingEntity(player, new S2C_SendEntityRotation(player.getId(), this.yBodyRot));
    }
}
