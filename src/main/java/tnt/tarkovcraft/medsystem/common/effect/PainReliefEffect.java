package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class PainReliefEffect extends SimpleStatusEffect {

    public static final MapCodec<PainReliefEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).apply(instance, PainReliefEffect::new));

    public PainReliefEffect(int duration) {
        super(duration);
    }

    public static PainReliefEffect createTemplate() {
        return new PainReliefEffect(-1);
    }

    @Override
    public StatusEffect copy() {
        return new PainReliefEffect(this.getDuration());
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.PAIN_RELIEF.value();
    }
}
