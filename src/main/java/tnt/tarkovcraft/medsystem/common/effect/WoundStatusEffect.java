package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class WoundStatusEffect extends SimpleStatusEffect {

    public static final MapCodec<WoundStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).apply(instance, WoundStatusEffect::new));

    public WoundStatusEffect(int duration) {
        super(duration);
    }

    public static WoundStatusEffect createTemplate() {
        return new WoundStatusEffect(20);
    }

    @Override
    public StatusEffect copy() {
        return new WoundStatusEffect(this.getDuration());
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.WOUND.value();
    }

    public static WoundStatusEffect mergeWithScaling(WoundStatusEffect first, WoundStatusEffect second) {
        int duration1 = first.getDuration();
        int duration2 = second.getDuration();
        return new WoundStatusEffect(duration1 + duration2 * 2);
    }
}
