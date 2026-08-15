package tnt.tarkovcraft.medsystem.common.interaction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

import java.util.Optional;

public final class EntityInteractionData {

    public static final Marker MARKER = MarkerManager.getMarker("EntityInteraction");
    public static final long INTERACTION_TTL = 1000L;
    public static final MapCodec<EntityInteractionData> CODEC = InteractionTracker.CODEC.optionalFieldOf("active_interaction")
            .xmap(EntityInteractionData::new, t -> Optional.ofNullable(t.activeInteraction));

    private @Nullable InteractionTracker activeInteraction;

    private EntityInteractionData(Optional<InteractionTracker> activeInteraction) {
        this.activeInteraction = activeInteraction.orElse(null);
    }

    public static EntityInteractionData create() {
        return new EntityInteractionData(Optional.empty());
    }

    public static EntityInteractionData getInteractionData(LivingEntity entity) {
        return entity.getData(MedSystemDataAttachments.INTERACTION_DATA);
    }

    public boolean isAnyInteractionActive() {
        return this.activeInteraction != null;
    }

    public boolean isInteractionActive(EntityInteractionType<?> interaction) {
        return this.activeInteraction != null && this.activeInteraction.interaction().type() == interaction;
    }

    public EntityInteraction getActiveInteraction() {
        return this.activeInteraction == null ? null : this.activeInteraction.interaction();
    }

    public boolean isInteractionReady(long gameTime) {
        if (this.activeInteraction == null)
            return false;
        return this.activeInteraction.isFinished(gameTime);
    }

    public boolean isInteractionExpired(long gameTime) {
        if (this.activeInteraction == null)
            return false;
        return this.activeInteraction.isExpired(gameTime);
    }

    public <T extends EntityInteraction> boolean startInteraction(EntityInteractionType<T> interaction, Player player, LivingEntity target, long initiationTime) {
        if (this.isAnyInteractionActive()) {
            MedicalSystem.LOGGER.warn(MARKER, "Skipping new interaction creation, an interaction is already active: {}", this.activeInteraction);
            return false;
        }
        T instance = interaction.createNewInteractionInstance(player, target);
        UserActionResult<Void> initiateResult = instance.checkAvailability(player, target);
        if (initiateResult.isFailure()) {
            MedicalSystem.LOGGER.debug(MARKER, "Interaction {} failed for player {} due to reason: {}", this.activeInteraction, player, initiateResult.message().getString());
            return false;
        }
        this.activeInteraction = InteractionTracker.create(initiationTime, instance);
        MedicalSystem.LOGGER.debug(MARKER, "Interaction {} started by player {} for {} with duration of {} ticks. Target timestamp: {}", this.activeInteraction, player, target, this.activeInteraction.interactionDuration, this.activeInteraction.targetTimestamp());
        return true;
    }

    public boolean finishInteraction(Player player, LivingEntity target) {
        return this.finishInteraction(player, target, false);
    }

    public boolean finishInteraction(Player player, LivingEntity target, boolean force) {
        if (!this.isAnyInteractionActive()) {
            MedicalSystem.LOGGER.warn(MARKER, "Failed to finish active interaction as there is no interaction active");
            return false;
        }
        long serverTime = player.level().getGameTime();
        if (this.activeInteraction.isFinished(serverTime) || force) {
            MedicalSystem.LOGGER.debug(MARKER, "Finishing interaction {} by player {} for {}", this.activeInteraction, player, target);
            this.activeInteraction.interaction.finishInteraction(player, target);
            this.activeInteraction = null;
            return true;
        }
        MedicalSystem.LOGGER.warn(MARKER, "Interaction {} is not finished yet, skipping finishing. Current timestamp: {}, target timestamp: {}", this.activeInteraction, serverTime, this.activeInteraction.targetTimestamp());
        return false;
    }

    public boolean cancelInteraction(Player origin, LivingEntity target) {
        MedicalSystem.LOGGER.debug(MARKER, "Cancelling interaction {} by player {} for {}", this.activeInteraction, origin, target);
        if (this.activeInteraction != null) {
            MedicalSystem.LOGGER.debug(MARKER, "Found active cancellable interaction {} by player {} for {}", this.activeInteraction, origin, target);
            this.activeInteraction.interaction.cancelInteraction(origin, target);
            this.activeInteraction = null;
            return true;
        }
        return false;
    }

    public void sync(LivingEntity entity) {
        if (entity.level().isClientSide())
            return;
        entity.syncData(MedSystemDataAttachments.INTERACTION_DATA);
    }

    private record InteractionTracker(EntityInteraction interaction, int interactionDuration, long interactionStartedAt) {

        static final Codec<InteractionTracker> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityInteraction.CODEC.fieldOf("interaction").forGetter(InteractionTracker::interaction),
                Codec.INT.optionalFieldOf("duration", 0).forGetter(InteractionTracker::interactionDuration),
                Codec.LONG.optionalFieldOf("initiatedAt", 0L).forGetter(InteractionTracker::interactionStartedAt)
        ).apply(instance, InteractionTracker::new));

        static InteractionTracker create(long initiationTime, EntityInteraction interaction) {
            int duration = interaction.getInteractionDuration();
            return new InteractionTracker(interaction, duration, initiationTime);
        }

        private boolean isFinished(long currentTime) {
            long remainingDuration = this.getRemainingDuration(currentTime);
            return remainingDuration <= 0 && Math.abs(remainingDuration) < INTERACTION_TTL;
        }

        private boolean isExpired(long currentTime) {
            long remainingDuration = this.getRemainingDuration(currentTime);
            return Math.abs(remainingDuration) >= INTERACTION_TTL;
        }

        private int getRemainingDuration(long currentTime) {
            long diff = currentTime - this.interactionStartedAt;
            return (int) (this.interactionDuration - diff);
        }

        private long targetTimestamp() {
            return this.interactionStartedAt + this.interactionDuration;
        }
    }
}
