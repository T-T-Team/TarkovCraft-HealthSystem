package tnt.tarkovcraft.medsystem.common.effect.util;

import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;

import java.util.List;

@FunctionalInterface
public interface StatusEffectSubmitter {

    StatusEffectSubmitter NOOP = (delay, tpl) -> {};

    void submit(int delay, StatusEffect template);

    default void submit(TickValue delay, StatusEffect template) {
        submit(delay.tickValue(), template);
    }

    default void submitImmediate(StatusEffect template) {
        submit(0, template);
    }

    static ListStatusEffectSubmitter list(List<PostEffect> list) {
        return new ListStatusEffectSubmitter(list);
    }

    static ListStatusEffectSubmitter list() {
        return new ListStatusEffectSubmitter();
    }
}
