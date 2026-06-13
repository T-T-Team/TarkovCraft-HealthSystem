package tnt.tarkovcraft.medsystem.common.effect.util;

import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;

import java.util.List;
import java.util.function.Consumer;

public interface StatusEffectSubmitter {

    StatusEffectSubmitter NOOP = Noop.INSTANCE;

    void submit(int delay, StatusEffect template);

    void clear();

    void accept(Consumer<StatusEffectWithDelay> consumer);

    default void submit(TickValue delay, StatusEffect template) {
        submit(delay.tickValue(), template);
    }

    default void submitImmediate(StatusEffect template) {
        submit(0, template);
    }

    static StatusEffectSubmitter list(List<StatusEffectWithDelay> list) {
        return new ListStatusEffectSubmitter(list);
    }

    static StatusEffectSubmitter list() {
        return new ListStatusEffectSubmitter();
    }

    enum Noop implements StatusEffectSubmitter {
        INSTANCE;
        @Override
        public void submit(int delay, StatusEffect template) {
        }
        @Override
        public void clear() {
        }
        @Override
        public void accept(Consumer<StatusEffectWithDelay> consumer) {
        }
    }
}
