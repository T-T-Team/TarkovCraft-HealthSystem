package tnt.tarkovcraft.medsystem.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteractions;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;

public record C2S_RescueDownedEntity(int entityId) implements CustomPacketPayload {

    public static final ResourceLocation PACKET_ID = MedicalSystemNetwork.createId(C2S_RescueDownedEntity.class);
    public static final Type<C2S_RescueDownedEntity> TYPE = new Type<>(PACKET_ID);
    public static final StreamCodec<ByteBuf, C2S_RescueDownedEntity> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, C2S_RescueDownedEntity::entityId,
            C2S_RescueDownedEntity::new
    );

    public void handleMessage(IPayloadContext ctx) {
        Player player = ctx.player();
        Level level = player.level();
        Entity entity = level.getEntity(this.entityId);
        if (entity instanceof LivingEntity livingEntity && HealthSystem.hasCustomHealth(entity) && BloodSystemManager.isUnconscious(livingEntity)) {
            UserActionResult<Void> result = EntityInteractions.evaluateInteraction(player, livingEntity, EntityInteractions.RESCUE_DOWNED);
            if (result.isSuccess()) {
                EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(livingEntity);
                bloodSystem.rescueDownedEntity(livingEntity);
                EntityInteractions.onInteractionCompletedCallback(player, livingEntity, EntityInteractions.RESCUE_DOWNED);
            } else {
                player.displayClientMessage(result.message(), true);
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
