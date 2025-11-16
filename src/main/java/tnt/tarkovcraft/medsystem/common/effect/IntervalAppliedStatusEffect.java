package tnt.tarkovcraft.medsystem.common.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

public abstract class IntervalAppliedStatusEffect extends StatusEffect {

    private final int updateInterval;

    public IntervalAppliedStatusEffect(int duration) {
        super(duration);
        this.updateInterval = this.getUpdateInterval();
    }

    public abstract int getUpdateInterval();

    public abstract void applyEffect(HealthContainer container, LivingEntity entity, @Nullable Limb limb);

    @Override
    public final void apply(HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        Level level = entity.level();
        long gameTime = level.getGameTime();
        if (this.updateInterval <= 0 || gameTime % this.updateInterval == 0) {
            this.applyEffect(container, entity, limb);
        }
    }
}
