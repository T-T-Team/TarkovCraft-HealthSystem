package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class HeavyBleedStatusEffect extends BleedStatusEffect {

    public static final MapCodec<HeavyBleedStatusEffect> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
            Codec.INT.fieldOf("duration").forGetter(BleedStatusEffect::getDuration)
    ).apply(instance, HeavyBleedStatusEffect::new));

    public HeavyBleedStatusEffect(int duration) {
        super(duration);
    }

    @Override
    public long getDamageInterval() {
        return 20L;
    }

    @Override
    public float getDamageAmount() {
        return 1.0F;
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.HEAVY_BLEED.value();
    }
}
