package tnt.tarkovcraft.medsystem.common.effect;

public enum EffectVisibility {

    NEVER,
    UI,
    ALWAYS;

    public boolean isVisibleInMode(EffectVisibility visibility) {
        return this.ordinal() >= visibility.ordinal();
    }
}
