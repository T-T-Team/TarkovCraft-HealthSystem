package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.*;

public final class MedSystemStatusEffects {

    public static final DeferredRegister<StatusEffectType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.STATUS_EFFECT, MedicalSystem.MOD_ID);

    public static final Holder<StatusEffectType<?>> PAIN_RELIEF = REGISTRY.register("pain_relief", key -> StatusEffectType.builder(key, (time, pwr) -> new PainReliefEffect(time))
            .persist(PainReliefEffect.CODEC)
            .type(EffectType.POSITIVE)
            .setGlobal()
            .build()
    );
    public static final Holder<StatusEffectType<?>> FRACTURE = REGISTRY.register("fracture", key -> StatusEffectType.builder(key, (time, pwr) -> FractureStatusEffect.INSTANCE)
            .persist(FractureStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .combineEffects((a, b) -> a)
            .build()
    );
    public static final Holder<StatusEffectType<?>> INJURY_RECOVERY = REGISTRY.register("injury_recovery", key -> StatusEffectType.builder(key, (time, pwr) -> new InjuryRecoveryStatusEffect(time, 1))
            .persist(InjuryRecoveryStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .combineEffects(InjuryRecoveryStatusEffect::merge)
            .build()
    );
    public static final Holder<StatusEffectType<?>> LIGHT_BLEED = REGISTRY.register("light_bleed", key -> StatusEffectType.builder(key, (time, pwr) -> new LightBleedStatusEffect(time))
            .persist(LightBleedStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .build()
    );
    public static final Holder<StatusEffectType<?>> HEAVY_BLEED = REGISTRY.register("heavy_bleed", key -> StatusEffectType.builder(key, (time, pwr) -> new HeavyBleedStatusEffect(time))
            .persist(HeavyBleedStatusEffect.CODEC)
            .type(EffectType.NEGATIVE)
            .build()
    );
}
