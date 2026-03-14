package tnt.tarkovcraft.medsystem.common.effect;

public abstract class SimpleStatusEffect extends StatusEffect {

    public SimpleStatusEffect(int duration) {
        super(duration);
    }

    @Override
    public final void apply(StatusEffectContext context) {
    }

    @Override
    public final void onRemoved(StatusEffectContext context) {
    }
}
