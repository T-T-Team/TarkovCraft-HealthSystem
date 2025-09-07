package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.*;
import tnt.tarkovcraft.medsystem.common.health.BodyPartGroup;

import java.util.Collections;

public final class MedSystemStatusEffects {

    public static final DeferredRegister<StatusEffectType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.STATUS_EFFECT, MedicalSystem.MOD_ID);

    public static final Holder<StatusEffectType<?>> PAIN_RELIEF = REGISTRY.register("pain_relief", key -> StatusEffectType.builder(key, PainReliefEffect::new)
            .persist(PainReliefEffect.CODEC)
            .type(EffectType.POSITIVE)
            .setGlobal()
            .combineEffects((a, b) -> StatusEffect.replace(a, b, PainReliefEffect::new))
            .build()
    );
    public static final Holder<StatusEffectType<?>> FRACTURE = REGISTRY.register("fracture", key -> StatusEffectType.builder(key, FractureStatusEffect::new)
            .persist(FractureStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .combineEffects((a, b) -> a)
            .ignoresBodyParts(BodyPartGroup.HEAD, BodyPartGroup.TORSO, BodyPartGroup.STOMACH)
            .build()
    );
    public static final Holder<StatusEffectType<?>> INJURY_RECOVERY = REGISTRY.register("injury_recovery", key -> StatusEffectType.builder(key, InjuryRecoveryStatusEffect::new)
            .persist(InjuryRecoveryStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .visibility(EffectVisibility.UI)
            .combineEffects(InjuryRecoveryStatusEffect::merge)
            .build()
    );
    public static final Holder<StatusEffectType<?>> LIGHT_BLEED = REGISTRY.register("light_bleed", key -> StatusEffectType.builder(key, LightBleedStatusEffect::new)
            .persist(LightBleedStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .build()
    );
    public static final Holder<StatusEffectType<?>> HEAVY_BLEED = REGISTRY.register("heavy_bleed", key -> StatusEffectType.builder(key, HeavyBleedStatusEffect::new)
            .persist(HeavyBleedStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .build()
    );
    public static final Holder<StatusEffectType<?>> FRESH_WOUND = REGISTRY.register("fresh_wound", key -> StatusEffectType.builder(key, FreshWoundStatusEffect::new)
            .persist(FreshWoundStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .build()
    );
    public static final Holder<StatusEffectType<?>> OVERWEIGHT = REGISTRY.register("overweight", key -> StatusEffectType.builder(key, (duration, delay) -> new OverweightStatusEffect())
            .persist(OverweightStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .setGlobal()
            .combineEffects((a, b) -> a)
            .build()
    );
    public static final Holder<StatusEffectType<?>> MAX_OVERWEIGHT = REGISTRY.register("max_overweight", key -> StatusEffectType.builder(key, (duration, delay) -> new MaxOverweightStatusEffect())
            .persist(MaxOverweightStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .setGlobal()
            .combineEffects((a, b) -> a)
            .build()
    );
    public static final Holder<StatusEffectType<?>> POSITIVE_EFFECTS_GROUP = REGISTRY.register("positive_effects_group", key -> StatusEffectType.builder(key, (duration, delay) -> new PositiveEffectsGroup(Collections.emptyList()))
            .persist(PositiveEffectsGroup.CODEC)
            .type(EffectType.POSITIVE)
            .setGlobal()
            .combineEffects(GroupStatusEffect::merge)
            .build()
    );
    public static final Holder<StatusEffectType<?>> NEUTRAL_EFFECTS_GROUP = REGISTRY.register("neutral_effects_group", key -> StatusEffectType.builder(key, (duration, delay) -> new NeutralEffectsGroup(Collections.emptyList()))
            .persist(NeutralEffectsGroup.CODEC)
            .type(EffectType.NEUTRAL)
            .setGlobal()
            .combineEffects(GroupStatusEffect::merge)
            .build()
    );
    public static final Holder<StatusEffectType<?>> NEGATIVE_EFFECTS_GROUP = REGISTRY.register("negative_effects_group", key -> StatusEffectType.builder(key, (duration, delay) -> new NegativeEffectsGroup(Collections.emptyList()))
            .persist(NegativeEffectsGroup.CODEC)
            .type(EffectType.NEGATIVE)
            .setGlobal()
            .combineEffects(GroupStatusEffect::merge)
            .build()
    );
}
