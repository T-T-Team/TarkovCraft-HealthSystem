package tnt.tarkovcraft.medsystem.common.effect.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;

public record StatusEffectWithDelay(int delay, StatusEffect template) {

    public static final Codec<StatusEffectWithDelay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("delay", 0).forGetter(StatusEffectWithDelay::delay),
            StatusEffectType.CODEC.fieldOf("template").forGetter(StatusEffectWithDelay::template)
    ).apply(instance, StatusEffectWithDelay::new));

    public StatusEffectWithDelay(TickValue delay, StatusEffect template) {
        this(delay.tickValue(), template);
    }

    public StatusEffectWithDelay(StatusEffect template) {
        this(0, template);
    }

    public StatusEffect createInstance() {
        return this.template.copy();
    }
}
