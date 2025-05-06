package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class LightBleedStatusEffect extends BleedStatusEffect {

    public static final MapCodec<LightBleedStatusEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            Codec.INT.fieldOf("duration").forGetter(BleedStatusEffect::getDuration)
    ).apply(instance, LightBleedStatusEffect::new));

    public LightBleedStatusEffect(int duration) {
        super(duration);
    }

    @Override
    public long getDamageInterval() {
        return 50L;
    }

    @Override
    public float getDamageAmount() {
        return 1.0F;
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.LIGHT_BLEED.value();
    }
}
