package tnt.tarkovcraft.medsystem.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;

public record S2C_SendEntityRotation(int entityId, float yBodyRot) implements CustomPacketPayload {

    public static final Identifier PACKET_ID = MedicalSystemNetwork.createId(S2C_SendEntityRotation.class);
    public static final Type<S2C_SendEntityRotation> TYPE = new Type<>(PACKET_ID);
    public static final StreamCodec<ByteBuf, S2C_SendEntityRotation> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, S2C_SendEntityRotation::entityId,
            ByteBufCodecs.FLOAT, S2C_SendEntityRotation::yBodyRot,
            S2C_SendEntityRotation::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleMessage(IPayloadContext context) {
        Player sender = context.player();
        Entity entity = sender.level().getEntity(this.entityId);
        if (entity.getType() != EntityTypes.PLAYER)
            return;
        Player target = (Player) entity;
        MedSystemConfig config = MedicalSystem.getConfig();
        if (!config.forceEntityRotationSynchronization)
            return;
        target.setYBodyRot(this.yBodyRot);
    }
}
