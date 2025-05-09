package tnt.tarkovcraft.medsystem.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tnt.tarkovcraft.medsystem.client.screen.SelectBodyPartScreen;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;

public record S2C_OpenBodyPartSelectScreen() implements CustomPacketPayload {

    public static final ResourceLocation PACKET_ID = MedicalSystemNetwork.createId(S2C_OpenBodyPartSelectScreen.class);
    public static final Type<S2C_OpenBodyPartSelectScreen> TYPE = new Type<>(PACKET_ID);
    public static final StreamCodec<ByteBuf, S2C_OpenBodyPartSelectScreen> CODEC = StreamCodec.unit(new S2C_OpenBodyPartSelectScreen());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleMessage(IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getMainHandItem();
        if (!stack.isEmpty() && player.level().isClientSide()) {
            this.handleScreenOpening();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void handleScreenOpening() {
        Minecraft client = Minecraft.getInstance();
        client.setScreen(new SelectBodyPartScreen());
    }
}
