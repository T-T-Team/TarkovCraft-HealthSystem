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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.util.context.ContextImpl;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.BloodEvent;
import tnt.tarkovcraft.medsystem.common.effect.*;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.*;

import java.util.Optional;

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

    public static final ResourceLocation VANILLA_ATTRIBUTE = MedicalSystem.resource("unconscious");
    public static final Pose UNCONSCIOUS_POSE = Pose.SWIMMING;
    public static final float DEATH_LIMIT = 0.50F; // 2.5L
    public static final float UNCONSCIOUS_LIMIT = 0.65F; // 3.25L
    public static final float MODERATE_BLOOD_LOSS = 0.80F; // 4.0L
    public static final float MILD_BLOOD_LOSS = 0.90F; // 4.5L

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
                // TODO event on wake up
                this.updateEffects(entity);
                this.updateConsciousStatus(entity, true);
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

    public boolean isUnconscious() {
        return this.unconsciousTime > 0;
    }

    public void setUnconsciousTime(int unconsciousTime) {
        this.unconsciousTime = unconsciousTime;
        this.changed = true;
    }

    public void setBloodVolume(float bloodVolume) {
        this.bloodVolume = Mth.clamp(bloodVolume, 0.0F, this.maxBloodVolume);
        this.changed = true;
    }

    public void updateEffects(LivingEntity entity) {
        if (entity.level().isClientSide() || !entity.isAlive())
            return;
        HealthContainer container = HealthSystem.getHealthData(entity);
        ServerLevel level = (ServerLevel) entity.level();
        float value = this.getBloodVolumePercentage();
        BloodStatus status = BloodStatus.HEALTHY;
        if (value < DEATH_LIMIT) {
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
            this.setUnconsciousTime(Math.max(this.unconsciousTime, 300));
            status = BloodStatus.DEATH;
        } else if (value < UNCONSCIOUS_LIMIT) {
            this.addBloodLossStatusEffect(container, entity, false);
            status = BloodStatus.UNCONSCIOUS;
            this.setUnconsciousTime(Math.max(this.unconsciousTime, 100));
        } else if (value < MODERATE_BLOOD_LOSS) {
            this.addBloodLossStatusEffect(container, entity, false);
            status = BloodStatus.MODERATE_BLOOD_LOSS;
        } else if (value < MILD_BLOOD_LOSS) {
            this.addBloodLossStatusEffect(container, entity, true);
            status = BloodStatus.MILD_BLOOD_LOSS;
        }
        NeoForge.EVENT_BUS.post(new BloodEvent.EffectUpdating(entity, this, status, value));
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
            if (unconscious && !entity.isPassenger()) {
                player.setForcedPose(UNCONSCIOUS_POSE);
            }
        }
        HealthContainer container = HealthSystem.getHealthData(entity);
        StatusEffectMap effects = container.getGlobalStatusEffects();
        AttributeMap attributeMap = entity.getAttributes();
        if (unconscious) {
            this.addModifier(attributeMap, Attributes.MOVEMENT_SPEED);
            this.addModifier(attributeMap, Attributes.JUMP_STRENGTH);
            this.addModifier(attributeMap, Attributes.STEP_HEIGHT);
            this.addModifier(attributeMap, Attributes.ATTACK_SPEED);
            this.addModifier(attributeMap, Attributes.BLOCK_BREAK_SPEED);
            this.addModifier(attributeMap, Attributes.BLOCK_INTERACTION_RANGE);
            if (!effects.hasEffect(MedSystemStatusEffects.UNCONSCIOUS)) {
                StatusEffectHelper.addEffect(effects, entity, null, new UnconsciousStatusEffect());
            }
        } else {
            this.removeModifier(attributeMap, Attributes.MOVEMENT_SPEED);
            this.removeModifier(attributeMap, Attributes.JUMP_STRENGTH);
            this.removeModifier(attributeMap, Attributes.STEP_HEIGHT);
            this.removeModifier(attributeMap, Attributes.ATTACK_SPEED);
            this.removeModifier(attributeMap, Attributes.BLOCK_BREAK_SPEED);
            this.removeModifier(attributeMap, Attributes.BLOCK_INTERACTION_RANGE);
            StatusEffectHelper.removeEffect(effects, entity, null, ContextImpl.empty(), MedSystemStatusEffects.UNCONSCIOUS);
        }
    }

    private void addModifier(AttributeMap map, Holder<Attribute> attribute) {
        AttributeInstance instance = map.getInstance(attribute);
        if (!instance.hasModifier(VANILLA_ATTRIBUTE)) {
            instance.addPermanentModifier(new AttributeModifier(VANILLA_ATTRIBUTE, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    private void removeModifier(AttributeMap map, Holder<Attribute> attribute) {
        AttributeInstance instance = map.getInstance(attribute);
        if (instance.hasModifier(VANILLA_ATTRIBUTE)) {
            instance.removeModifier(VANILLA_ATTRIBUTE);
        }
    }
}
