package tnt.tarkovcraft.medsystem.common.effect;

import net.minecraft.world.level.Level;

public abstract class IntervalAppliedStatusEffect extends StatusEffect {

    private final int updateInterval;

    public IntervalAppliedStatusEffect(int duration) {
        super(duration);
        this.updateInterval = this.getUpdateInterval();
    }

    public abstract int getUpdateInterval();

    public abstract void applyEffect(StatusEffectContext context);

    @Override
    public final void apply(StatusEffectContext context) {
        Level level = context.level();
        long gameTime = level.getGameTime();
        if (this.updateInterval <= 0 || gameTime % this.updateInterval == 0) {
            this.applyEffect(context);
        }
    }
}
