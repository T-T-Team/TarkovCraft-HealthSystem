package tnt.tarkovcraft.medsystem.common.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;

public abstract class IntervalAppliedStatusEffect extends StatusEffect {

    private final int updateInterval;

    public IntervalAppliedStatusEffect(int duration) {
        super(duration);
        this.updateInterval = this.getUpdateInterval();
    }

    public abstract int getUpdateInterval();

    public abstract void applyEffect(LivingEntity entity, Context context);

    @Override
    public final void apply(Context context) {
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
        Level level = entity.level();
        long gameTime = level.getGameTime();
        if (this.updateInterval <= 0 || gameTime % this.updateInterval == 0) {
            this.applyEffect(entity, context);
        }
    }
}
