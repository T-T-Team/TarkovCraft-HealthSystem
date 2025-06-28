package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.*;
import tnt.tarkovcraft.medsystem.common.health.BodyPartGroup;

public final class MedSystemStatusEffects {

    public static final DeferredRegister<StatusEffectType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.STATUS_EFFECT, MedicalSystem.MOD_ID);

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
}
