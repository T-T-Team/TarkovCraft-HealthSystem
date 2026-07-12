package tnt.tarkovcraft.medsystem.common.blood_system.assignment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.api.AttachmentSyncCallbackListener;
import tnt.tarkovcraft.core.api.client.SynchronizableScreen;
import tnt.tarkovcraft.core.client.TarkovCraftCoreClient;
import tnt.tarkovcraft.core.util.Cached;
import tnt.tarkovcraft.core.util.EventHandler;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.*;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

import java.util.Map;
import javax.annotation.Nullable;

public final class EntityBloodSystem {

    public static final Codec<EntityBloodSystem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity_type").forGetter(t -> t.type),
            ResourceLocation.CODEC.fieldOf("blood_type").forGetter(t -> t.bloodType),
            Codec.FLOAT.fieldOf("blood_volume").forGetter(t -> t.bloodVolume),
            UnconsciousState.CODEC.optionalFieldOf("unconscious_state", UnconsciousState.createConscious()).forGetter(t -> t.unconsciousState),
            Codec.FLOAT.optionalFieldOf("shock_amount", 0.0F).forGetter(t -> t.shockAmount)
    ).apply(instance, EntityBloodSystem::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityBloodSystem> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
    public static final SynchronizableScreen.DataSource BLOOD_SYSTEM = new SynchronizableScreen.DataSource(MedicalSystem.createIdentifier("blood_system"));

    private final EntityType<?> type;
    private final ResourceLocation bloodType;
    private float bloodVolume;
    private final UnconsciousState unconsciousState;
    private float shockAmount;

    private final Cached<EntityBloodSystemDefinition> definition;
    private boolean synchronizationNeeded;
    public final EventHandler<BloodSystemListener> eventHandler;

    EntityBloodSystem(EntityType<?> type, ResourceLocation bloodType, float bloodVolume) {
        this(type, bloodType, bloodVolume, UnconsciousState.createConscious(), 0.0F);
    }

    private EntityBloodSystem(EntityType<?> type, ResourceLocation bloodType, float bloodVolume, UnconsciousState unconsciousState, float shockAmount) {
        this.type = type;
        this.bloodType = bloodType;
        this.bloodVolume = bloodVolume;
        this.unconsciousState = unconsciousState;
        this.shockAmount = shockAmount;

        this.definition = Cached.create(this::loadDefinition);
        this.eventHandler = EventHandler.create();
        this.unconsciousState.addListener(new UnconsciousListener(this));
    }

    public static EntityBloodSystem invalid(IAttachmentHolder holder) {
        if (holder instanceof Entity entity) {
            return new EntityBloodSystem(entity.getType(), null, 0);
        }
        return null;
    }

    public static @Nullable EntityBloodSystem getAttached(LivingEntity entity) {
        return entity.getExistingDataOrNull(MedSystemDataAttachments.BLOOD_SYSTEM);
    }

    public static void detach(LivingEntity entity) {
        entity.removeData(MedSystemDataAttachments.BLOOD_SYSTEM);
    }

    public void tick(LivingEntity entity) {
        this.bloodTick(entity);
        this.unconsciousState.tick(entity);
        this.shockTick(entity);

        if (this.synchronizationNeeded) {
            this.synchronizationNeeded = false;
            this.synchronizeImmediately(entity);
        }
    }

    public void addShock(float amount) {
        this.shockAmount = Mth.clamp(this.shockAmount + amount, 0.0F, 1.5F);
        this.markForUpdate();
    }

    public void removeShock(float amount) {
        this.shockAmount = Math.max(0, this.shockAmount - amount);
        this.markForUpdate();
    }

    public float getShockAmount() {
        return shockAmount;
    }

    public boolean isValidBloodAttachment(LivingEntity entity) {
        EntityBloodSystemDefinition definition = EntityBloodSystemDefinition.forEntity(entity);
        return definition != null && definition.canUseBloodType(this.bloodType);
    }

    public ResourceLocation getBloodType() {
        return this.bloodType;
    }

    public boolean isUnconscious() {
        return this.getDefinition().isUnconsciousModeAllowed() && this.unconsciousState.isUnconscious();
    }

    public UnconsciousState getUnconsciousState() {
        return this.unconsciousState;
    }

    public void setUnconscious(LivingEntity entity, int durationTicks, UnconsciousOptions options) {
        this.setUnconscious(entity, durationTicks, options, false);
    }

    public void setUnconscious(LivingEntity entity, int durationTicks, UnconsciousOptions options, boolean force) {
        Map<String, Float> metadata = null;
        if (!this.isUnconscious()) {
            EntityBloodSystemDefinition definition = this.getDefinition();
            RandomSource randomSource = entity.getRandom();
            metadata = definition.calculateUnconsciousPose(randomSource);
        }
        this.unconsciousState.setUnconscious(durationTicks, options, force, metadata);
        this.markForUpdate();
    }

    public void setOrExtendedUnconscious(LivingEntity entity, int durationTicks, UnconsciousOptions options) {
        this.setUnconscious(entity, Math.max(this.unconsciousState.getRemainingUnconsciousDuration(), durationTicks), options);
    }

    public float causeBloodLoss(float amount) {
        float loseAmount = Math.min(this.bloodVolume, amount);
        this.bloodVolume = this.bloodVolume - loseAmount;
        this.markForUpdate();
        return loseAmount;
    }

    public float extractBlood(float requestedAmount) {
        return this.causeBloodLoss(requestedAmount);
    }

    public float performTransfusion(LivingEntity entity, float transfusionAmount, ResourceLocation bloodType) {
        float amount = Math.min(this.getMissingBloodVolumeForTransfusion(), transfusionAmount);
        this.recoverBlood(amount);
        ResourceLocation myBloodType = this.getBloodType();
        BloodConfiguration configuration = MedicalSystem.BLOOD_SYSTEM.getConfig();
        if (!entity.level().isClientSide() && !configuration.isCompatibleBloodTypeForTransfusion(myBloodType, bloodType)) {
            this.eventHandler.dispatch(listener -> listener.onIncompatibleBloodTransfusion(entity, this.bloodType, bloodType, amount));
        }
        return amount;
    }

    public float getMissingBloodVolumeForTransfusion() {
        return Math.max(this.getDefinition().getMaxBloodVolume() - this.bloodVolume, 0.0F);
    }

    public boolean hasBledOut() {
        return this.bloodVolume <= 0;
    }

    public void setBloodVolume(float volume) {
        this.bloodVolume = Mth.clamp(volume, 0.0F, this.getDefinition().getMaxBloodVolume());
        this.markForUpdate();
    }

    public void recoverBlood(float amount) {
        this.setBloodVolume(this.bloodVolume + amount);
    }

    public float getBloodVolume() {
        return this.bloodVolume;
    }

    public boolean isInPain() {
        EntityBloodSystemDefinition definition = this.getDefinition();
        float limit = definition.getMaxBloodVolume();
        float f = Mth.clamp(this.bloodVolume / limit, 0.0F, 1.0F);
        return definition.isInPain(f);
    }

    public void markForUpdate() {
        this.synchronizationNeeded = true;
    }

    public void synchronizeImmediately(LivingEntity entity) {
        this.synchronizationNeeded = false;
        entity.syncData(MedSystemDataAttachments.BLOOD_SYSTEM);
    }

    public EntityBloodSystemDefinition getDefinition() {
        return this.definition.get();
    }

    public void rescueDownedEntity(LivingEntity entity) {
        this.setUnconscious(entity, 100, UnconsciousOptions.RESCUE_DELAY);
        this.shockAmount = 0.0F;
    }

    public @Nullable UnconsciousAnimationState getUnconsciousAnimationState(float delta) {
        return this.isUnconscious() ? this.unconsciousState.calculateAnimationState(delta) : null;
    }

    private EntityBloodSystemDefinition loadDefinition() {
        return EntityBloodSystemDefinition.forEntityType(this.type);
    }

    private void bloodTick(LivingEntity entity) {
        Level level = entity.level();
        long gameTime = level.getGameTime();
        if (gameTime % 20 != 0L)
            return;
        boolean bleeding = HealthSystem.isBleeding(entity);
        EntityBloodSystemDefinition definition = this.getDefinition();
        float maxBloodVolume = definition.getMaxBloodVolume();
        // regeneration
        if (!bleeding && this.bloodVolume < maxBloodVolume) {
            float recoveryAmount = Math.max(0, definition.getBloodRegenerationAmount(entity));
            this.recoverBlood(recoveryAmount);
        }
        // bloodloss status effect
        this.eventHandler.dispatch(listener -> listener.onBloodTick(this.bloodVolume, entity, definition));
        if (!level.isClientSide())
            definition.applyEffects(entity, (ServerLevel) level, this);
    }

    private void shockTick(LivingEntity entity) {
        if (this.shockAmount > 0) {
            EntityBloodSystemDefinition definition = this.getDefinition();
            boolean isUnconscious = this.isUnconscious();
            boolean inShock = definition.isInShock(isUnconscious, this.shockAmount);
            if (inShock && (!isUnconscious || this.unconsciousState.getUnconsciousOptions() == UnconsciousOptions.PAIN_SHOCK)) {
                this.setOrExtendedUnconscious(entity, 50, UnconsciousOptions.PAIN_SHOCK);
            }
            float recoveryRate = definition.getShockRecoveryRate(inShock);
            this.shockAmount = Mth.clamp(this.shockAmount - recoveryRate, 0.0F, 1.0F);
        }
    }

    public static final class SyncHandler implements AttachmentSyncHandler<EntityBloodSystem>, AttachmentSyncCallbackListener<EntityBloodSystem> {

        @Override
        public void write(RegistryFriendlyByteBuf buf, EntityBloodSystem attachment, boolean initialSync) {
            STREAM_CODEC.encode(buf, attachment);
        }

        @Override
        public @Nullable EntityBloodSystem read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable EntityBloodSystem previousValue) {
            return STREAM_CODEC.decode(buf);
        }

        @Override
        public void onDataSynced(IAttachmentHolder holder, AttachmentType<EntityBloodSystem> attachmentType, EntityBloodSystem attachment) {
            TarkovCraftCoreClient.synchronizeCurrentScreen(BLOOD_SYSTEM);
        }
    }

    private record UnconsciousListener(EntityBloodSystem bloodSystem) implements UnconsciousState.Listener {

        @Override
        public void onUnconsciousStateChanged(LivingEntity entity, boolean unconscious) {
            UnconsciousModeHelper.onChanged(unconscious, entity, this.bloodSystem);
            this.bloodSystem.markForUpdate();
        }

        @Override
        public void onWakeUp(LivingEntity entity, UnconsciousOptions options, int totalUnconsciousDuration) {
            if (options.allowRescue()) { // entity was in rescue mode and was not rescued
                this.bloodSystem.causeBloodLoss(Float.MAX_VALUE);
                this.bloodSystem.setOrExtendedUnconscious(entity, 50, UnconsciousOptions.DOWNED_NO_RESCUE);
            } else {
                this.bloodSystem.unconsciousState.setInvulnerableDuration(100);
            }
            this.bloodSystem.markForUpdate();
        }
    }
}
