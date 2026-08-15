package tnt.tarkovcraft.medsystem.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteractionData;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteractionType;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;

import java.util.function.IntFunction;

public record C2S_RequestInteractionState(EntityInteractionType<?> interactionType, State state, int entityId, long clientTimeStamp) implements CustomPacketPayload {

    public static final ResourceLocation PACKET_ID = MedicalSystemNetwork.createId(C2S_RequestInteractionState.class);
    public static final Type<C2S_RequestInteractionState> TYPE = new Type<>(PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, C2S_RequestInteractionState> CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(MedSystemRegistries.Keys.ENTITY_INTERACTION), C2S_RequestInteractionState::interactionType,
            State.STREAM_CODEC, C2S_RequestInteractionState::state,
            ByteBufCodecs.INT, C2S_RequestInteractionState::entityId,
            ByteBufCodecs.LONG, C2S_RequestInteractionState::clientTimeStamp,
            C2S_RequestInteractionState::new
    );
    private static final long TIME_DIFF_TOLERANCE = 30L; // 1.5s for possible network delays etc

    public static C2S_RequestInteractionState start(EntityInteractionType<?> interactionType, LivingEntity entity, long clientTimeStamp) {
        return new C2S_RequestInteractionState(interactionType, State.START, entity.getId(), clientTimeStamp);
    }

    public static C2S_RequestInteractionState finish(EntityInteractionType<?> interactionType, LivingEntity entity) {
        return new C2S_RequestInteractionState(interactionType, State.FINISH, entity.getId(), 0L);
    }

    public static C2S_RequestInteractionState cancel(EntityInteractionType<?> interactionType, LivingEntity entity) {
        return new C2S_RequestInteractionState(interactionType, State.CANCEL, entity.getId(), 0L);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleMessage(IPayloadContext context) {
        Player player = context.player();
        Level level = player.level();
        Entity entity = level.getEntity(this.entityId);
        if (!(entity instanceof LivingEntity livingEntity))
            return;
        EntityInteractionData interactionData = EntityInteractionData.getInteractionData(player);
        MedicalSystem.LOGGER.debug(EntityInteractionData.MARKER, "Received interaction {} state for {}: {}", this.interactionType, livingEntity, this.state);
        long serverTimeStamp = level.getGameTime();
        // Interaction start
        if (this.state == State.START) {
            if (interactionData.isAnyInteractionActive()) {
                MedicalSystem.LOGGER.debug(EntityInteractionData.MARKER, "Received interaction {} state for {} while another interaction is active", this.interactionType, livingEntity);
                return;
            }
            if (Math.abs(serverTimeStamp - this.clientTimeStamp) > TIME_DIFF_TOLERANCE) {
                MedicalSystem.LOGGER.debug(EntityInteractionData.MARKER, "Received outdated interaction state for {}. Expected: {}, Actual: {}", livingEntity, this.clientTimeStamp, serverTimeStamp);
                return;
            }
            if (!interactionData.startInteraction(this.interactionType, player, livingEntity, this.clientTimeStamp)) {
                MedicalSystem.LOGGER.debug(EntityInteractionData.MARKER, "Failed to start interaction {} for {}", this.interactionType, livingEntity);
            }
        } else if (interactionData.isInteractionActive(this.interactionType)) {
            // Interaction completion - check if interaction has not expired first
            if (interactionData.isInteractionExpired(serverTimeStamp) || this.state == State.CANCEL) {
                if (!interactionData.cancelInteraction(player, livingEntity)) {
                    MedicalSystem.LOGGER.debug(EntityInteractionData.MARKER, "Failed to cancel interaction {} for {}", this.interactionType, livingEntity);
                }
            } else {
                if (!interactionData.finishInteraction(player, livingEntity)) {
                    MedicalSystem.LOGGER.debug(EntityInteractionData.MARKER, "Failed to finish interaction {} for {}", this.interactionType, livingEntity);
                }
            }
        }
        interactionData.sync(player);
    }

    public enum State {

        START,
        FINISH,
        CANCEL;

        private static final IntFunction<State> BY_ID = ByIdMap.continuous(State::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, State> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
    }
}
