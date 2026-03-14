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
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.util.Cached;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.BloodSystemEvent;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodConfiguration;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousModeHelper;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousOptions;
import tnt.tarkovcraft.medsystem.common.effect.BloodImmuneReactionStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.BloodLossStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public final class EntityBloodSystem {

    public static final MapCodec<EntityBloodSystem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("entity_type").forGetter(t -> t.type),
            Identifier.CODEC.fieldOf("blood_type").forGetter(t -> t.bloodType),
            Codec.FLOAT.fieldOf("blood_volume").forGetter(t -> t.bloodVolume),
            Codec.INT.optionalFieldOf("remaining_unconscious_time", 0).forGetter(t -> t.remainingUnconsciousTime),
            UnconsciousOptions.CODEC.optionalFieldOf("unconscious_options", UnconsciousOptions.EMPTY).forGetter(t -> t.unconsciousOptions)
    ).apply(instance, EntityBloodSystem::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityBloodSystem> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    private final EntityType<?> type;
    private final Identifier bloodType;
    private float bloodVolume;
    private int remainingUnconsciousTime;
    private int unconsciousInvulnerability;
    private UnconsciousOptions unconsciousOptions;

    private final Cached<EntityBloodSystemDefinition> definition;
    private Boolean lastUnconsciousState;
    private boolean synchronizationNeeded;

    EntityBloodSystem(EntityType<?> type, Identifier bloodType, float bloodVolume) {
        this(type, bloodType, bloodVolume, 0, UnconsciousOptions.EMPTY);
    }

    private EntityBloodSystem(EntityType<?> type, Identifier bloodType, float bloodVolume, int remainingUnconsciousTime, UnconsciousOptions options) {
        this.type = type;
        this.bloodType = bloodType;
        this.bloodVolume = bloodVolume;
        this.remainingUnconsciousTime = remainingUnconsciousTime;
        this.unconsciousOptions = options;

        this.definition = Cached.create(this::loadDefinition);
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

        if (this.synchronizationNeeded) {
            this.synchronizationNeeded = false;
            this.synchronizeImmediately(entity);
        }
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
            this.initiateBloodImmuneReaction(entity);
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
        if (HealthSystem.hasCustomHealth(entity)) {
            BloodLossStatusEffect.Stage stage = definition.getBloodLossStage(this.bloodVolume / maxBloodVolume);
            HealthContainer container = HealthSystem.getHealthData(entity);
            StatusEffectMap statusEffects = container.getGlobalStatusEffects();
            BloodLossStatusEffect statusEffect = (BloodLossStatusEffect) statusEffects.getEffect(MedSystemStatusEffects.BLOODLOSS)
                    .orElse(null);
            if (stage != null && (statusEffect == null || statusEffect.getStage() != stage)) {
                BloodLossStatusEffect bloodLoss = BloodLossStatusEffect.createTemplate(stage);
                StatusEffectHelper.addGlobalEffect(statusEffects, entity, 1, bloodLoss);
            }
        }
        if (!level.isClientSide())
            definition.applyEffects(entity, (ServerLevel) level, this);
    }

    private void unconsciousTick(LivingEntity entity) {
        boolean unconscious = this.isUnconscious();
        if (this.lastUnconsciousState == null || unconscious != this.lastUnconsciousState) {
            UnconsciousModeHelper.onChanged(unconscious, entity, this);
            this.markForUpdate();
        }
        if (this.unconsciousInvulnerability > 0 && !this.unconsciousOptions.allowRescue()) {
            --this.unconsciousInvulnerability;
            return;
        }
        if (this.remainingUnconsciousTime > 0 && --this.remainingUnconsciousTime <= 0) {
            this.unconsciousOptions = UnconsciousOptions.EMPTY;
            // rescue time out, cause death
            if (this.unconsciousOptions.allowRescue()) {
                this.causeBloodLoss(Float.MAX_VALUE);
            } else {
                this.setUnconsciousPrevention(100);
            }

        }
        this.lastUnconsciousState = unconscious;
    }

    private void initiateBloodImmuneReaction(LivingEntity entity) {
        if (!HealthSystem.hasCustomHealth(entity))
            return;
        HealthContainer container = HealthSystem.getHealthData(entity);
        StatusEffectMap effects = container.getGlobalStatusEffects();
        int delay = Duration.minutes(5).tickValue();
        StatusEffectHelper.addGlobalEffect(effects, entity, delay, BloodImmuneReactionStatusEffect.createDefault());
    }
}
