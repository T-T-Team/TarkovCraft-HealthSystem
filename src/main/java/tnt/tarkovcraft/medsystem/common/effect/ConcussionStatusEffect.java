package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class ConcussionStatusEffect extends SimpleStatusEffect {

    public static final MapCodec<ConcussionStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).apply(instance, ConcussionStatusEffect::new));

    public ConcussionStatusEffect(int duration) {
        super(duration);
    }

    public static ConcussionStatusEffect createTemplate() {
        return new ConcussionStatusEffect(-1);
    }

    @Override
    public StatusEffect copy() {
        return new ConcussionStatusEffect(this.getDuration());
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.CONCUSSION.value();
    }
}
