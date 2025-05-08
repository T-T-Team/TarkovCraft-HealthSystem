package tnt.tarkovcraft.medsystem.common.effect;

import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.core.util.context.Context;

public interface StatusEffect {

    StatusEffectType<?> getType();

    void apply(Context context);

    void onRemoved(Context context);

    int getDuration();

    void setDuration(int duration);

    int getPower();

    StatusEffect copy();

    default void setDuration(TickValue duration) {
        this.setDuration(duration.tickValue());
    }

    default void addDuration(int duration) {
        this.setDuration(this.getDuration() + duration);
    }

    default void addDuration(TickValue duration) {
        this.setDuration(this.getDuration() + duration.tickValue());
    }

    default boolean isInfinite() {
        return this.getDuration() < 0;
    }

    static <S extends StatusEffect> S merge(S a, S b) {
        if (a.getPower() > b.getPower()) {
            return a;
        }
        if (b.getPower() > a.getPower()) {
            return b;
        }
        if (a.isInfinite() || b.isInfinite()) {
            a.setDuration(-1);
        } else {
            int duration = a.getDuration();
            a.setDuration(duration + b.getDuration());
        }
        return a;
    }
}
