package tnt.tarkovcraft.medsystem.common.status;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.init.CoreAttributes;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.BloodEvent;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.config.UnconsciousMode;
import tnt.tarkovcraft.medsystem.common.effect.MildBloodLossStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.ModerateBloodLossStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.UnconsciousStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.*;

import java.util.Optional;
import java.util.UUID;

public final class BloodData {

    public static final MapCodec<BloodData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("maxBloodVolume").forGetter(t -> t.maxBloodVolume),
            Codec.FLOAT.fieldOf("bloodVolume").forGetter(t -> t.bloodVolume),
            Codec.INT.optionalFieldOf("unconsciousTime", 0).forGetter(t -> t.unconsciousTime)
    ).apply(instance, BloodData::new));
    public static final StreamCodec<ByteBuf, BloodData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, t -> t.maxBloodVolume,
            ByteBufCodecs.FLOAT, t -> t.bloodVolume,
            ByteBufCodecs.INT, t -> t.unconsciousTime,
            BloodData::new
    );

    public static final ResourceLocation ATTR_UNCONSCIOUS = MedicalSystem.resource("unconscious");
    public static final ResourceLocation ATTR_DEBUFF = MedicalSystem.resource("blood_debuff");
    public static final UUID UUID_DEBUFF = UUID.fromString("6079d919-84b8-4e8b-9639-bbfd8d313ee1");
    public static final Pose UNCONSCIOUS_POSE = Pose.SWIMMING;
    public static final EntityDimensions PLAYER_UNCONSCIOUS_DIMENSIONS = EntityDimensions.scalable(1.4F, 0.4F);

    private final float maxBloodVolume;
    private float bloodVolume;
    private int unconsciousTime;
    private boolean changed;

    public BloodData(float maxBloodVolume) {
        this(maxBloodVolume, maxBloodVolume, 0);
    }

    private BloodData(float maxBloodVolume, float bloodVolume, int unconsciousTime) {
        this.maxBloodVolume = maxBloodVolume;
        this.bloodVolume = bloodVolume;
        this.unconsciousTime = unconsciousTime;
    }

    public void update(LivingEntity entity) {
        Level level = entity.level();
        this.updateConsciousStatus(entity, false);
        if (this.changed) {
            this.updateEffects(entity);
            this.changed = false;
            this.sync(entity);
        }
        long time = level.getGameTime();
        if (time % 20L == 0L) {
            this.bloodLevelTick(entity);
        }
        if (this.isUnconscious()) {
            if (--this.unconsciousTime <= 0) {
                BloodEvent.OnWakeUp onWakeUp = NeoForge.EVENT_BUS.post(new BloodEvent.OnWakeUp(entity, this));
                if (onWakeUp.willWakeUp()) {
                    this.updateEffects(entity);
                    this.updateConsciousStatus(entity, true);
                } else {
                    this.setUnconsciousTime(onWakeUp.getUnconsciousTime());
                }
            }
        }
    }

    public float getBloodVolume() {
        return bloodVolume;
    }

    public float getBloodVolumePercentage() {
        return Mth.clamp(this.bloodVolume / this.maxBloodVolume, 0.0F, 1.0F);
    }

    public float getMaxBloodVolume() {
        return maxBloodVolume;
    }

    public float getMissingBloodVolume() {
        return this.maxBloodVolume - this.bloodVolume;
    }

    public boolean hasFullBloodVolume() {
        return this.bloodVolume >= this.maxBloodVolume;
    }

    public boolean isUnconscious() {
        return this.unconsciousTime > 0;
    }

    public void setUnconsciousTime(int unconsciousTime) {
        this.unconsciousTime = unconsciousTime;
        this.changed = true;
    }

    public void setOrExtendedUnconsciousTime(int unconsciousTime) {
        this.setUnconsciousTime(Math.max(this.unconsciousTime, unconsciousTime));
    }

    public void setBloodVolume(float bloodVolume) {
        this.bloodVolume = Mth.clamp(bloodVolume, 0.0F, this.maxBloodVolume);
        this.changed = true;
    }

    public float extract(float requested) {
        float extractionAmount = Math.min(this.bloodVolume, requested);
        this.setBloodVolume(this.bloodVolume - extractionAmount);
        return extractionAmount;
    }

    public float insert(float requested) {
        float insertionAmount = Math.min(this.getMissingBloodVolume(), requested);
        this.setBloodVolume(this.bloodVolume + insertionAmount);
        return insertionAmount;
    }

    public void updateEffects(LivingEntity entity) {
        if (entity.level().isClientSide() || !entity.isAlive())
            return;
        HealthContainer container = HealthSystem.getHealthData(entity);
        ServerLevel level = (ServerLevel) entity.level();
        float value = this.getBloodVolumePercentage();
        BloodStatus status = BloodStatus.fromBloodLevelPercentage(value);
        status.applyEffects(this, entity, level, container);
        NeoForge.EVENT_BUS.post(new BloodEvent.BloodEffectsTick(entity, this, status, value));
    }

    public void sync(LivingEntity entity) {
        if (entity.isAlive())
            entity.syncData(MedSystemDataAttachments.BLOOD_DATA);
    }

    private void bloodLevelTick(LivingEntity entity) {
        if (this.bloodVolume >= this.maxBloodVolume) {
            return;
        }
        HealthContainer container = HealthSystem.getHealthData(entity);
        boolean bleeding = container.hasMatchingStatusEffect(MedSystemTags.StatusEffects.IS_BLEED);
        boolean changed = false;
        if (!bleeding) {
            float bloodRecoverySpeed = AttributeSystem.getFloatValue(entity, MedSystemAttributes.BLOOD_REGENERATION_AMOUNT, 0.0F);
            if (bloodRecoverySpeed > 0.0F) {
                changed = true;
                this.setBloodVolume(this.bloodVolume + bloodRecoverySpeed);
            }
        }
        if (changed) {
            this.updateEffects(entity);
        }
    }

    public void onDeathBloodLevel(LivingEntity entity, ServerLevel level, HealthContainer container) {
        StatusEffect effect = container.getStatusEffectStream()
                .filter(statusEffect -> statusEffect.getType().is(MedSystemTags.StatusEffects.IS_BLEED))
                .findAny().orElse(null);
        RegistryAccess access = level.registryAccess();
        if (effect != null) {
            entity.hurtServer(level, MedSystemDamageTypes.causeBleedDamage(access, effect.getCausingEntity(level)), 4.0F);
        } else {
            entity.hurtServer(level, MedSystemDamageTypes.causeBleedDamage(access, Optional.empty()), 4.0F);
        }
        this.addBloodLossStatusEffect(container, entity, false);
        this.setOrExtendedUnconsciousTime(300);
    }

    public void onUnconsciousBloodLevel(LivingEntity entity, ServerLevel level, HealthContainer container) {
        this.addBloodLossStatusEffect(container, entity, false);
        this.setOrExtendedUnconsciousTime(100);

        MedSystemConfig config = MedicalSystem.getConfig();
        UnconsciousMode mode = config.unconsciousMode;
        if (!mode.allowsUnconsciousState(level)) {
            this.onDeathBloodLevel(entity, level, container);
        }
    }

    public void onRandomBlackoutBloodLevel(LivingEntity entity, ServerLevel level, HealthContainer container) {
        this.addBloodLossStatusEffect(container, entity, false);
        float chance = AttributeSystem.getFloatValue(entity, MedSystemAttributes.RANDOM_BLACKOUT_CHANCE, 0.05F);
        RandomSource random = level.getRandom();
        if (chance > 0.0F && random.nextFloat() < chance) {
            this.setOrExtendedUnconsciousTime(100 + random.nextInt(200));
        }

        AttributeMap map = entity.getAttributes();
        this.addModifier(map, Attributes.MOVEMENT_SPEED, ATTR_DEBUFF, -0.3F, true);

        AttributeSystem.addModifier(entity, CoreAttributes.WEIGHT_LIMIT, tnt.tarkovcraft.core.common.attribute.modifier.AttributeModifier.multiplier(UUID_DEBUFF, 0.5), true);
    }

    public void onModerateBloodLoss(LivingEntity entity, ServerLevel level, HealthContainer container) {
        this.addBloodLossStatusEffect(container, entity, false);

        AttributeMap map = entity.getAttributes();
        this.addModifier(map, Attributes.MOVEMENT_SPEED, ATTR_DEBUFF, -0.2F, true);

        AttributeSystem.addModifier(entity, CoreAttributes.WEIGHT_LIMIT, tnt.tarkovcraft.core.common.attribute.modifier.AttributeModifier.multiplier(UUID_DEBUFF, 0.75), true);
    }

    public void onMildBloodLoss(LivingEntity entity, ServerLevel level, HealthContainer container) {
        this.addBloodLossStatusEffect(container, entity, true);

        AttributeMap map = entity.getAttributes();
        this.addModifier(map, Attributes.MOVEMENT_SPEED, ATTR_DEBUFF, -0.1F, true);

        AttributeSystem.addModifier(entity, CoreAttributes.WEIGHT_LIMIT, tnt.tarkovcraft.core.common.attribute.modifier.AttributeModifier.multiplier(UUID_DEBUFF, 0.9), true);
    }

    public void onClearDebuffData(LivingEntity entity, ServerLevel level, HealthContainer container) {
        AttributeMap map = entity.getAttributes();
        this.removeModifier(map, ATTR_DEBUFF, Attributes.MOVEMENT_SPEED);

        AttributeSystem.removeModifier(entity, CoreAttributes.WEIGHT_LIMIT, UUID_DEBUFF);
    }

    private void addBloodLossStatusEffect(HealthContainer container, LivingEntity entity, boolean mild) {
        StatusEffectHelper.addEffect(container.getGlobalStatusEffects(), entity, null, mild ? new MildBloodLossStatusEffect() : new ModerateBloodLossStatusEffect());
        HealthSystem.synchronizeEntity(entity);
    }

    private void updateConsciousStatus(LivingEntity entity, boolean wakeUp) {
        if (!entity.isAlive())
            return;
        boolean unconscious = this.isUnconscious();
        if (entity instanceof Player player) {
            if (wakeUp) {
                player.setForcedPose(null);
            }
            if (unconscious && !entity.isPassenger() && player.getForcedPose() == null) {
                player.setForcedPose(UNCONSCIOUS_POSE);
            }
        }
        HealthContainer container = HealthSystem.getHealthData(entity);
        StatusEffectMap effects = container.getGlobalStatusEffects();
        AttributeMap attributeMap = entity.getAttributes();
        if (unconscious) {
            this.addUnconsciousModifier(attributeMap, Attributes.MOVEMENT_SPEED);
            this.addUnconsciousModifier(attributeMap, Attributes.JUMP_STRENGTH);
            this.addUnconsciousModifier(attributeMap, Attributes.STEP_HEIGHT);
            this.addUnconsciousModifier(attributeMap, Attributes.ATTACK_SPEED);
            this.addUnconsciousModifier(attributeMap, Attributes.BLOCK_BREAK_SPEED);
            this.addUnconsciousModifier(attributeMap, Attributes.BLOCK_INTERACTION_RANGE);
            if (!effects.hasEffect(MedSystemStatusEffects.UNCONSCIOUS)) {
                StatusEffectHelper.addEffect(effects, entity, null, new UnconsciousStatusEffect());
            }
            if (entity.isUsingItem())
                entity.stopUsingItem();
        } else {
            this.removeUnconsciousModifier(attributeMap, Attributes.MOVEMENT_SPEED);
            this.removeUnconsciousModifier(attributeMap, Attributes.JUMP_STRENGTH);
            this.removeUnconsciousModifier(attributeMap, Attributes.STEP_HEIGHT);
            this.removeUnconsciousModifier(attributeMap, Attributes.ATTACK_SPEED);
            this.removeUnconsciousModifier(attributeMap, Attributes.BLOCK_BREAK_SPEED);
            this.removeUnconsciousModifier(attributeMap, Attributes.BLOCK_INTERACTION_RANGE);
            StatusEffectHelper.removeEffect(StatusEffectSubmitter.NOOP, effects, entity, null, container, MedSystemStatusEffects.UNCONSCIOUS);
        }
    }

    private void addModifier(AttributeMap map, Holder<Attribute> attribute, ResourceLocation id, double value, boolean replace) {
        AttributeInstance instance = map.getInstance(attribute);
        if (!instance.hasModifier(id)) {
            instance.addTransientModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (replace) {
            instance.addOrUpdateTransientModifier(new AttributeModifier(id, value, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private void addUnconsciousModifier(AttributeMap map, Holder<Attribute> attribute) {
        this.addModifier(map, attribute, ATTR_UNCONSCIOUS, -1.0, false);
    }

    private void removeModifier(AttributeMap map, ResourceLocation id, Holder<Attribute> attribute) {
        AttributeInstance instance = map.getInstance(attribute);
        if (instance.hasModifier(id)) {
            instance.removeModifier(id);
        }
    }

    private void removeUnconsciousModifier(AttributeMap map, Holder<Attribute> attribute) {
        this.removeModifier(map, ATTR_UNCONSCIOUS, attribute);
    }
}
