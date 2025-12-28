package tnt.tarkovcraft.medsystem.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tnt.tarkovcraft.medsystem.client.ClientNetworkHandler;
import tnt.tarkovcraft.medsystem.common.item.InteractionTarget;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;

public record S2C_OpenLimbSelectScreen(boolean selfHealing, int entityId) implements CustomPacketPayload {

    public static final ResourceLocation PACKET_ID = MedicalSystemNetwork.createId(S2C_OpenLimbSelectScreen.class);
    public static final Type<S2C_OpenLimbSelectScreen> TYPE = new Type<>(PACKET_ID);
    public static final StreamCodec<ByteBuf, S2C_OpenLimbSelectScreen> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, S2C_OpenLimbSelectScreen::selfHealing,
            ByteBufCodecs.INT, S2C_OpenLimbSelectScreen::entityId,
            S2C_OpenLimbSelectScreen::new
    );

    public S2C_OpenLimbSelectScreen(InteractionTarget.Mutable interaction) {
        this(interaction.isSelf(), interaction.getEntityId());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleMessage(IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getMainHandItem();
        if (!stack.isEmpty() && player.level().isClientSide()) {
            ClientNetworkHandler.openLimbSelectionScreen(this.selfHealing, this.entityId);
        }
    }
}
