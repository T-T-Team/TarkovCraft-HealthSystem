package tnt.tarkovcraft.medsystem.common.effect.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;

public record StatusEffectWithDelay(int delay, StatusEffect template) {

    public static final Codec<StatusEffectWithDelay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NumberProvider.NON_NEGATIVE_INT.optionalFieldOf("delay", 0).forGetter(StatusEffectWithDelay::delay),
            StatusEffectType.CODEC.fieldOf("template").forGetter(StatusEffectWithDelay::template)
    ).apply(instance, StatusEffectWithDelay::new));

    public StatusEffectWithDelay(StatusEffect template) {
        this(0, template);
    }

    public StatusEffect createInstance() {
        return this.template.copy();
    }

    public int getDelay() {
        return Math.max(this.delay, 0);
    }
}
