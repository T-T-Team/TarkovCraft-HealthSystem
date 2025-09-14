package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class WoundStatusEffect extends StatusEffect {

    public static final MapCodec<WoundStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).apply(instance, WoundStatusEffect::new));

    public WoundStatusEffect(int duration) {
        super(duration);
    }

    public static WoundStatusEffect createTemplate() {
        return new WoundStatusEffect(20);
    }

    @Override
    public void apply(Context context) {
    }

    @Override
    public StatusEffect onRemoved(Context context) {
        return null;
    }

    @Override
    public StatusEffect copy() {
        return new WoundStatusEffect(this.getDuration());
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.WOUND.value();
    }
}
