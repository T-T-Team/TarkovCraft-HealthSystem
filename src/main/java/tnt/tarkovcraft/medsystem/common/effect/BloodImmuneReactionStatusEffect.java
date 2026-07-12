package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageTypes;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public final class BloodImmuneReactionStatusEffect extends IntervalAppliedStatusEffect {

    public static final float DEFAULT_UNCONSCIOUS_THRESHOLD = 0.85F;
    public static final MapCodec<BloodImmuneReactionStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).and(instance.group(
            Codec.FLOAT.fieldOf("progression_rate").forGetter(t -> t.progressionRate),
            Codec.FLOAT.optionalFieldOf("unconscious_threshold", DEFAULT_UNCONSCIOUS_THRESHOLD).forGetter(t -> t.unconsciousThreshold),
            Codec.FLOAT.optionalFieldOf("reaction_progress", 0.0F).forGetter(t -> t.reactionProgress)
    )).apply(instance, BloodImmuneReactionStatusEffect::new));

    private final float progressionRate;
    private final float unconsciousThreshold;
    private float reactionProgress;

    public static BloodImmuneReactionStatusEffect createDefault() {
        return new BloodImmuneReactionStatusEffect(INFINITE_DURATION);
    }

    public BloodImmuneReactionStatusEffect(int duration) {
        this(duration, 0.0015F); // 0.09 per minute
    }

    public BloodImmuneReactionStatusEffect(int duration, float progressionRate) {
        this(duration, progressionRate, DEFAULT_UNCONSCIOUS_THRESHOLD);
    }

    public BloodImmuneReactionStatusEffect(int duration, float progressionRate, float unconsciousThreshold) {
        super(duration);
        this.progressionRate = progressionRate;
        this.unconsciousThreshold = unconsciousThreshold;
    }

    private BloodImmuneReactionStatusEffect(int duration, float progressionRate, float unconsciousThreshold, float reactionProgress) {
        this(duration, progressionRate, unconsciousThreshold);
        this.reactionProgress = reactionProgress;
    }

    @Override
    public int getUpdateInterval() {
        return 20;
    }

    @Override
    public void applyEffect(StatusEffectContext context) {
        LivingEntity entity = context.entity();
        this.reactionProgress = Math.max(this.reactionProgress + this.progressionRate, 0.0F);
        if (context.isServerSide() && this.reactionProgress >= this.unconsciousThreshold && BloodSystemManager.isEnabled(entity)) {
            EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(entity);
            EntityBloodSystemDefinition definition = bloodSystem.getDefinition();
            if (definition.isUnconsciousModeAllowed() && (!bloodSystem.isUnconscious() || !bloodSystem.getUnconsciousState().getUnconsciousOptions().allowRescue())) {
                bloodSystem.setOrExtendedUnconscious(entity, 100, UnconsciousOptions.IMMUNE_REACTION);
            }
        }
        if (context.isServerSide() && this.reactionProgress >= 1.0F) {
            ServerLevel level = (ServerLevel) context.level();
            DamageSource damageSource = MedSystemDamageTypes.causeImmuneReactionDamage(entity.registryAccess());
            entity.hurtServer(level, damageSource, 2.0F * this.reactionProgress);
        }
    }

    @Override
    public void onRemoved(StatusEffectContext context) {
    }

    @Override
    public StatusEffect copy() {
        return new BloodImmuneReactionStatusEffect(this.getDuration(), this.progressionRate, this.unconsciousThreshold, this.reactionProgress);
    }

    public BloodImmuneReactionStatusEffect mergeImmuneEffect(BloodImmuneReactionStatusEffect newEffect) {
        float rate = Math.max(this.progressionRate, newEffect.progressionRate);
        float progress = this.reactionProgress + newEffect.reactionProgress;
        float threshold = Math.min(this.unconsciousThreshold, newEffect.unconsciousThreshold);
        int duration = sumEffectDurations(this, newEffect);
        return new BloodImmuneReactionStatusEffect(duration, rate, threshold, progress);
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.BLOOD_IMMUNE_REACTION.value();
    }
}
