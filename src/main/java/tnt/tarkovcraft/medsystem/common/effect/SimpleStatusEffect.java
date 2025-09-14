package tnt.tarkovcraft.medsystem.common.effect;

import tnt.tarkovcraft.core.util.context.Context;

public abstract class SimpleStatusEffect extends StatusEffect {

    public SimpleStatusEffect(int duration) {
        super(duration);
    }

    @Override
    public final void apply(Context context) {
    }

    @Override
    public final StatusEffect onRemoved(Context context) {
        return null;
    }
}
