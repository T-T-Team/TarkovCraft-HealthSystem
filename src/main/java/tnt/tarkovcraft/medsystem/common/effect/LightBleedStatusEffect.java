package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.Optional;
import java.util.UUID;

public class LightBleedStatusEffect extends BleedStatusEffect {

    public static final MapCodec<LightBleedStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> commonEntity(instance).apply(instance, LightBleedStatusEffect::new));

    public LightBleedStatusEffect(int duration, int delay, Optional<UUID> owner) {
        super(duration, delay);
    }

    public LightBleedStatusEffect(int duration, int delay) {
        super(duration, delay);
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
    public StatusEffect copy() {
        return new LightBleedStatusEffect(this.getDuration(), this.getDelay());
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.LIGHT_BLEED.value();
    }
}
