package tnt.tarkovcraft.medsystem.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.heal.HealItemAttributes;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.common.item.InteractionTarget;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;

public record C2S_SelectLimb(InteractionTarget target) implements CustomPacketPayload {

    public static final ResourceLocation PACKET_ID = MedicalSystemNetwork.createId(C2S_SelectLimb.class);
    public static final Type<C2S_SelectLimb> TYPE = new Type<>(PACKET_ID);
    public static final StreamCodec<ByteBuf, C2S_SelectLimb> CODEC = StreamCodec.composite(
            InteractionTarget.STREAM_CODEC, C2S_SelectLimb::target,
            C2S_SelectLimb::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleMessage(IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getMainHandItem();
        HealItemAttributes attributes = stack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
        LivingEntity targetEntity = this.getTargetEntity(player.level(), player);
        if (targetEntity == null) {
            MedicalSystem.LOGGER.warn("Could not find target entity for healing by entity ID");
            return;
        }
        if (!HealthSystem.hasCustomHealth(targetEntity)) {
            MedicalSystem.LOGGER.warn("Target entity \"{}\" does not have custom health container", targetEntity);
            return;
        }
        HealthContainer container = HealthContainer.getAttached(targetEntity);
        Limb limb = container.getLimbByCode(this.target.limbCode());
        if (attributes != null && attributes.canUseOnLimb(limb, stack, container, this.target.self(), targetEntity)) {
            stack.set(MedSystemItemComponents.INTERACTION_TARGET, this.target);
        }
    }

    private LivingEntity getTargetEntity(Level level, LivingEntity healer) {
        if (target.self()) {
            return healer;
        } else {
            int entityId = target.entityId();
            Entity entity = level.getEntity(entityId);
            if (entity instanceof LivingEntity targetEntity) {
                return targetEntity;
            }
        }
        return null;
    }
}
