package tnt.tarkovcraft.medsystem.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tnt.tarkovcraft.medsystem.api.HealAttributes;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;

public record C2S_SelectBodyPart(String bodyPart) implements CustomPacketPayload {

    public static final ResourceLocation PACKET_ID = MedicalSystemNetwork.createId(C2S_SelectBodyPart.class);
    public static final Type<C2S_SelectBodyPart> TYPE = new Type<>(PACKET_ID);
    public static final StreamCodec<ByteBuf, C2S_SelectBodyPart> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, C2S_SelectBodyPart::bodyPart,
            C2S_SelectBodyPart::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleMessage(IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getMainHandItem();
        HealthContainer container = HealthSystem.getHealthData(player);
        HealAttributes attributes = stack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
        BodyPart part = container.getBodyPart(this.bodyPart);
        if (attributes != null && attributes.canUseOnPart(part, player, container)) {
            stack.set(MedSystemItemComponents.SELECTED_BODY_PART, this.bodyPart);
        }
    }
}
