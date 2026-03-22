package tnt.tarkovcraft.medsystem.common.blood_system.assignment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.util.Cached;
import tnt.tarkovcraft.core.util.EventHandler;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.BloodSystemEvent;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodConfiguration;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousModeHelper;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousOptions;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

public final class EntityBloodSystem {

    public static final MapCodec<EntityBloodSystem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity_type").forGetter(t -> t.type),
            Identifier.CODEC.fieldOf("blood_type").forGetter(t -> t.bloodType),
            Codec.FLOAT.fieldOf("blood_volume").forGetter(t -> t.bloodVolume),
            Codec.INT.optionalFieldOf("remaining_unconscious_time", 0).forGetter(t -> t.remainingUnconsciousTime),
            UnconsciousOptions.CODEC.optionalFieldOf("unconscious_options", UnconsciousOptions.EMPTY).forGetter(t -> t.unconsciousOptions),
            Codec.FLOAT.optionalFieldOf("shock_amount", 0.0F).forGetter(t -> t.shockAmount)
    ).apply(instance, EntityBloodSystem::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityBloodSystem> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    private final EntityType<?> type;
    private final Identifier bloodType;
    private float bloodVolume;
    private int remainingUnconsciousTime;
    private int unconsciousInvulnerability;
    private UnconsciousOptions unconsciousOptions;
    private float shockAmount;

    private final Cached<EntityBloodSystemDefinition> definition;
    private Boolean lastUnconsciousState;
    private boolean synchronizationNeeded;
    public final EventHandler<BloodSystemListener> eventHandler;

    EntityBloodSystem(EntityType<?> type, Identifier bloodType, float bloodVolume) {
        this(type, bloodType, bloodVolume, 0, UnconsciousOptions.EMPTY, 0.0F);
    }

    private EntityBloodSystem(EntityType<?> type, Identifier bloodType, float bloodVolume, int remainingUnconsciousTime, UnconsciousOptions options, float shockAmount) {
        this.type = type;
        this.bloodType = bloodType;
        this.bloodVolume = bloodVolume;
        this.remainingUnconsciousTime = remainingUnconsciousTime;
        this.unconsciousOptions = options;
        this.shockAmount = shockAmount;

        this.definition = Cached.create(this::loadDefinition);
        this.eventHandler = EventHandler.create();
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
        this.unconsciousTick(entity);
        this.shockTick();

        if (this.synchronizationNeeded) {
            this.synchronizationNeeded = false;
            this.synchronizeImmediately(entity);
        }
    }

    public void addShock(float amount) {
        this.shockAmount = Math.max(0, this.shockAmount + amount);
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

    public Identifier getBloodType() {
        return this.bloodType;
    }

    public boolean isUnconscious() {
        return this.getDefinition().isUnconsciousModeAllowed() && this.remainingUnconsciousTime > 0 && (this.unconsciousInvulnerability <= 0 || this.unconsciousOptions.allowRescue());
    }

    public int getRemainingUnconsciousTime() {
        return remainingUnconsciousTime;
    }

    public UnconsciousOptions getActiveUnconsciousModeOptions() {
        return !this.isUnconscious() ? UnconsciousOptions.EMPTY : this.unconsciousOptions;
    }

    public void setUnconscious(int durationTicks, UnconsciousOptions options) {
        this.remainingUnconsciousTime = Math.max(0, durationTicks);
        this.unconsciousOptions = options;
        this.markForUpdate();
    }

    public void setOrExtendedUnconscious(int durationTicks, UnconsciousOptions options) {
        this.setUnconscious(Math.max(this.remainingUnconsciousTime, durationTicks), options);
    }

    public void setUnconsciousPrevention(int duration) {
        this.unconsciousInvulnerability = Math.max(0, duration);
        this.markForUpdate();
    }

    public void setOrExtendedUnconsciousPrevention(int duration) {
        this.setUnconsciousPrevention(Math.max(this.unconsciousInvulnerability, duration));
    }

    public void rescueDownedEntity(LivingEntity entity, LivingEntity rescuer, ItemStack stack) {
        this.setUnconscious(100, UnconsciousOptions.RESCUE_DELAY);
        NeoForge.EVENT_BUS.post(new BloodSystemEvent.EntityRescued(entity, this, rescuer, stack));
    }

    public void causeBloodLoss(float amount) {
        this.bloodVolume = Math.max(0, this.bloodVolume - amount);
        this.markForUpdate();
    }

    public float extractBlood(float requestedAmount) {
        float extractedAmount = Math.min(this.bloodVolume, requestedAmount);
        this.causeBloodLoss(extractedAmount);
        return extractedAmount;
    }

    public float performTransfusion(LivingEntity entity, float transfusionAmount, Identifier bloodType) {
        float amount = Math.min(this.getMissingBloodVolumeForTransfusion(), transfusionAmount);
        this.recoverBlood(amount);
        Identifier myBloodType = this.getBloodType();
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

    // TODO 26.1 use immutable itemstack
    public boolean canRescueUnconsciousEntity(LivingEntity entity, LivingEntity rescuer, ItemStack stack) {
        if (!this.isUnconscious() || !this.getActiveUnconsciousModeOptions().allowRescue()) {
            return false;
        }
        BloodSystemEvent.EntityRescueAttempt rescueAttempt = NeoForge.EVENT_BUS.post(new BloodSystemEvent.EntityRescueAttempt(entity, this, rescuer, stack));
        return rescueAttempt.canRescue();
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

    private void unconsciousTick(LivingEntity entity) {
        boolean unconscious = this.remainingUnconsciousTime > 0;
        if (this.lastUnconsciousState == null || unconscious != this.lastUnconsciousState) {
            UnconsciousModeHelper.onChanged(unconscious, entity, this);
            this.markForUpdate();
        }
        this.lastUnconsciousState = unconscious;
        if (this.unconsciousInvulnerability > 0 && !this.unconsciousOptions.allowRescue()) {
            --this.unconsciousInvulnerability;
            return;
        }
        if (this.remainingUnconsciousTime > 0 && --this.remainingUnconsciousTime <= 0) {
            // rescue time out, cause death
            if (this.unconsciousOptions.allowRescue()) {
                this.causeBloodLoss(Float.MAX_VALUE);
                this.setOrExtendedUnconscious(30, UnconsciousOptions.BLOODLOSS);
            } else {
                this.unconsciousOptions = UnconsciousOptions.EMPTY;
                this.setUnconsciousPrevention(100);
            }
        }
    }

    private void shockTick() {
        if (this.shockAmount > 0) {
            EntityBloodSystemDefinition definition = this.getDefinition();
            boolean inShock = definition.isInShock(this.shockAmount);
            if (inShock && (!this.isUnconscious() || this.unconsciousOptions == UnconsciousOptions.PAIN_SHOCK)) {
                this.setOrExtendedUnconscious(50, UnconsciousOptions.PAIN_SHOCK);
            }
            float recoveryRate = definition.getShockRecoveryRate(inShock);
            this.shockAmount = Mth.clamp(this.shockAmount - recoveryRate, 0.0F, 1.0F);
        }
    }
}
