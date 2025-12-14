package tnt.tarkovcraft.medsystem.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;

public record S2C_RefreshEntityDimensions(int id) implements CustomPacketPayload {

    public static final Identifier PACKET_ID = MedicalSystemNetwork.createId(S2C_RefreshEntityDimensions.class);
    public static final Type<S2C_RefreshEntityDimensions> TYPE = new Type<>(PACKET_ID);
    public static final StreamCodec<ByteBuf, S2C_RefreshEntityDimensions> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, S2C_RefreshEntityDimensions::id,
            S2C_RefreshEntityDimensions::new
    );

    public static void broadcast(LivingEntity entity) {
        if (entity.level().isClientSide())
            return;
        S2C_RefreshEntityDimensions packet = new S2C_RefreshEntityDimensions(entity.getId());
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
    }

    public void handleMessage(IPayloadContext context) {
        Player player = context.player();
        Level level = player.level();
        Entity entity = level.getEntity(this.id);
        if (entity != null && entity.isAlive()) {
            entity.refreshDimensions();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
