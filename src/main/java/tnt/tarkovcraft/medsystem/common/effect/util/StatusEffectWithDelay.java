package tnt.tarkovcraft.medsystem.common.effect.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.core.common.data.number.ConstantNumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;

public record StatusEffectWithDelay(NumberProvider delay, StatusEffect template) {

    public static final Codec<StatusEffectWithDelay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            NumberProviderType.CODEC.optionalFieldOf("delay", ConstantNumberProvider.ZERO).forGetter(StatusEffectWithDelay::delay),
            StatusEffectType.CODEC.fieldOf("template").forGetter(StatusEffectWithDelay::template)
    ).apply(instance, StatusEffectWithDelay::new));

    public StatusEffectWithDelay(TickValue delay, StatusEffect template) {
        this(ConstantNumberProvider.of(delay.tickValue()), template);
    }

    public StatusEffectWithDelay(StatusEffect template) {
        this(ConstantNumberProvider.ZERO, template);
    }

    public StatusEffect createInstance() {
        return this.template.copy();
    }

    public int getDelay() {
        int value = this.delay.intValue();
        return Math.max(value, 0);
    }
}
