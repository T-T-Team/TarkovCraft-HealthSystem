package tnt.tarkovcraft.medsystem.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;

public record C2S_RequestGiveUp() implements CustomPacketPayload {

    public static final Identifier PACKET_ID = MedicalSystemNetwork.createId(C2S_RequestGiveUp.class);
    public static final Type<C2S_RequestGiveUp> TYPE = new Type<>(PACKET_ID);
    public static final StreamCodec<ByteBuf, C2S_RequestGiveUp> CODEC = StreamCodec.unit(new C2S_RequestGiveUp());

    public void handleMessage(IPayloadContext ctx) {
        Player player = ctx.player();
        if (BloodSystem.canGiveUp(player)) {
            BloodSystem.causeBloodLoss(player, Float.MAX_VALUE);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
