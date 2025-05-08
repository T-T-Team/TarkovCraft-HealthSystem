package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class FractureStatusEffect extends StatusEffect {

    public static final MapCodec<FractureStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).apply(instance, FractureStatusEffect::new));

    public FractureStatusEffect(int duration, int delay) {
        super(duration, delay);
    }

    @Override
    public StatusEffect copy() {
        return new FractureStatusEffect(this.getDuration(), this.getDelay());
    }

    @Override
    public void apply(Context context) {
    }

    @Override
    public StatusEffect onRemoved(Context context) {
        return null;
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.FRACTURE.value();
    }
}
