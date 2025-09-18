package tnt.tarkovcraft.medsystem.common.effect;

import tnt.tarkovcraft.core.util.context.Context;

import java.util.Collection;

public abstract class SimpleStatusEffect extends StatusEffect {

    public SimpleStatusEffect(int duration) {
        super(duration);
    }

    @Override
    public final void apply(Context context) {
    }

    @Override
    public final Collection<PostEffect> onRemoved(Context context) {
        return null;
    }
}
