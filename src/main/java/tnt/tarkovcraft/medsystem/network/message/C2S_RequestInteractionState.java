package tnt.tarkovcraft.medsystem.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteractionData;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteractionType;
import tnt.tarkovcraft.medsystem.network.MedicalSystemNetwork;

import java.util.function.IntFunction;

public record C2S_RequestInteractionState(EntityInteractionType<?> interactionType, State state, int entityId) implements CustomPacketPayload {

    public static final Identifier PACKET_ID = MedicalSystemNetwork.createId(C2S_RequestInteractionState.class);
    public static final Type<C2S_RequestInteractionState> TYPE = new Type<>(PACKET_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, C2S_RequestInteractionState> CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(MedSystemRegistries.Keys.ENTITY_INTERACTION), C2S_RequestInteractionState::interactionType,
            State.STREAM_CODEC, C2S_RequestInteractionState::state,
            ByteBufCodecs.INT, C2S_RequestInteractionState::entityId,
            C2S_RequestInteractionState::new
    );

    public C2S_RequestInteractionState(EntityInteractionType<?> interactionType, State state, LivingEntity entity) {
        this(interactionType, state, entity.getId());
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
        if (interactionData.isInteractionActive(this.interactionType)) {
            long time = level.getGameTime();
            if (interactionData.isInteractionExpired(time) || this.state == State.CANCEL) {
                interactionData.cancelInteraction(player, livingEntity);
            } else if (this.state == State.START) {
                interactionData.startInteraction(this.interactionType, player, livingEntity);
            } else {
                interactionData.finishInteraction(player, livingEntity);
            }
            interactionData.sync(player);
        }
    }

    public enum State {

        START,
        FINISH,
        CANCEL;

        private static final IntFunction<State> BY_ID = ByIdMap.continuous(State::ordinal, values(), ByIdMap.OutOfBoundsStrategy.WRAP);
        public static final StreamCodec<ByteBuf, State> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
    }
}
