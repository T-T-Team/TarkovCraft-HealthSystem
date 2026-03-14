package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.effect.*;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectVisibility;

import java.util.Collections;
import java.util.Optional;

public final class MedSystemStatusEffects {

    public static final DeferredRegister<StatusEffectType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedSystemConstants.MOD_ID);

    public static final Holder<StatusEffectType<?>> PAIN_RELIEF = REGISTRY.register("pain_relief", key -> StatusEffectType.builder(key, PainReliefEffect::new)
            .persist(PainReliefEffect.CODEC)
            .type(EffectType.POSITIVE)
            .setGlobal()
            .combineEffects(StatusEffect::maxDuration)
            .build()
    );
    public static final Holder<StatusEffectType<?>> PAIN = REGISTRY.register("pain", key -> StatusEffectType.builder(key, PainStatusEffect::new)
            .persist(PainStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .setGlobal()
            .combineEffects(StatusEffect::replace)
            .setSpecial()
            .build()
    );
    public static final Holder<StatusEffectType<?>> FRACTURE = REGISTRY.register("fracture", key -> StatusEffectType.builder(key, FractureStatusEffect::new)
            .persist(FractureStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .combineEffects(StatusEffect::maxDuration)
            .healPriority(MedSystemConstants.HEAL_EFFECT_MAJOR)
            .build()
    );
    public static final Holder<StatusEffectType<?>> INJURY_RECOVERY = REGISTRY.register("injury_recovery", key -> StatusEffectType.builder(key, InjuryRecoveryStatusEffect::new)
            .persist(InjuryRecoveryStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .visibility(EffectVisibility.UI)
            .combineEffects(InjuryRecoveryStatusEffect::merge)
            .healPriority(MedSystemConstants.HEAL_EFFECT_MINOR)
            .build()
    );
    public static final Holder<StatusEffectType<?>> BLEED = REGISTRY.register("bleed", key -> StatusEffectType.builder(key, duration -> BleedStatusEffect.defaultLightBleed(duration, Optional.empty()))
            .persist(BleedStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .healPriority(MedSystemConstants.HEAL_EFFECT_CRITICAL)
            .combineEffects(BleedStatusEffect::withHighestDamage)
            .build()
    );
    public static final Holder<StatusEffectType<?>> FRESH_WOUND = REGISTRY.register("fresh_wound", key -> StatusEffectType.builder(key, FreshWoundStatusEffect::new)
            .persist(FreshWoundStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .healPriority(MedSystemConstants.HEAL_EFFECT_MINOR)
            .build()
    );
    public static final Holder<StatusEffectType<?>> OVERWEIGHT = REGISTRY.register("overweight", key -> StatusEffectType.builder(key, (duration) -> new OverweightStatusEffect(false))
            .persist(OverweightStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .setGlobal()
            .combineEffects(StatusEffect::keep)
            .setSpecial()
            .build()
    );
    public static final Holder<StatusEffectType<?>> POSITIVE_EFFECTS_GROUP = REGISTRY.register("positive_effects_group", key -> StatusEffectType.builder(key, (duration) -> new PositiveEffectsGroup(Collections.emptyList()))
            .persist(PositiveEffectsGroup.CODEC)
            .type(EffectType.POSITIVE)
            .setGlobal()
            .combineEffects(GroupStatusEffect::merge)
            .setSpecial()
            .build()
    );
    public static final Holder<StatusEffectType<?>> NEUTRAL_EFFECTS_GROUP = REGISTRY.register("neutral_effects_group", key -> StatusEffectType.builder(key, (duration) -> new NeutralEffectsGroup(Collections.emptyList()))
            .persist(NeutralEffectsGroup.CODEC)
            .type(EffectType.NEUTRAL)
            .setGlobal()
            .combineEffects(GroupStatusEffect::merge)
            .setSpecial()
            .build()
    );
    public static final Holder<StatusEffectType<?>> NEGATIVE_EFFECTS_GROUP = REGISTRY.register("negative_effects_group", key -> StatusEffectType.builder(key, (duration) -> new NegativeEffectsGroup(Collections.emptyList()))
            .persist(NegativeEffectsGroup.CODEC)
            .type(EffectType.NEGATIVE)
            .setGlobal()
            .combineEffects(GroupStatusEffect::merge)
            .setSpecial()
            .build()
    );
    public static final Holder<StatusEffectType<?>> WOUND = REGISTRY.register("wound", key -> StatusEffectType.builder(key, WoundStatusEffect::new)
            .persist(WoundStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .combineEffects(WoundStatusEffect::mergeWithScaling)
            .visibility(EffectVisibility.NEVER)
            .healPriority(MedSystemConstants.HEAL_EFFECT_MINOR)
            .setGlobal()
            .build()
    );
    public static final Holder<StatusEffectType<?>> UNCONSCIOUS = REGISTRY.register("unconscious", key -> StatusEffectType.builder(key, duration -> new UnconsciousStatusEffect())
            .persist(UnconsciousStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .visibility(EffectVisibility.NEVER)
            .setGlobal()
            .combineEffects(StatusEffect::replace)
            .setSpecial()
            .build()
    );
    public static final Holder<StatusEffectType<?>> CONCUSSION = REGISTRY.register("concussion", key -> StatusEffectType.builder(key, ConcussionStatusEffect::new)
            .persist(ConcussionStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .setGlobal()
            .healPriority(MedSystemConstants.HEAL_EFFECT_MINOR)
            .combineEffects(StatusEffect::maxDuration)
            .build()
    );
    public static final Holder<StatusEffectType<?>> BLOODLOSS = REGISTRY.register("bloodloss", key -> StatusEffectType.builder(key, BloodLossStatusEffect::new)
            .persist(BloodLossStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .setGlobal()
            .setSpecial()
            .combineEffects(StatusEffect::replace)
            .build()
    );
    public static final Holder<StatusEffectType<?>> BLOOD_IMMUNE_REACTION = REGISTRY.register("blood_immune_reaction", key -> StatusEffectType.builder(key, BloodImmuneReactionStatusEffect::new)
            .persist(BloodImmuneReactionStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .visibility(EffectVisibility.NEVER)
            .setGlobal()
            .setSpecial()
            .combineEffects(BloodImmuneReactionStatusEffect::mergeImmuneEffect)
            .build()
    );
}
