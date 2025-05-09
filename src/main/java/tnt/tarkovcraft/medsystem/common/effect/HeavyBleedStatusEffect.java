package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.Optional;
import java.util.UUID;

public class HeavyBleedStatusEffect extends BleedStatusEffect {

    public static final MapCodec<HeavyBleedStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> commonEntity(instance).apply(instance, HeavyBleedStatusEffect::new));

    public HeavyBleedStatusEffect(int duration, int delay, Optional<UUID> owner) {
        super(duration, delay, owner);
    }

    public HeavyBleedStatusEffect(int duration, int delay) {
        super(duration, delay);
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
    public StatusEffect copy() {
        return new HeavyBleedStatusEffect(this.getDuration(), this.getDelay());
    }

    @Override
    public StatusEffect onRemoved(Context context) {
        return new FreshWoundStatusEffect(
                Duration.minutes(5).tickValue(),
                Duration.seconds(5).tickValue()
        );
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.HEAVY_BLEED.value();
    }
}
