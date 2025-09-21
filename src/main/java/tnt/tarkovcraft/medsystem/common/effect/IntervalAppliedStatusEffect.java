package tnt.tarkovcraft.medsystem.common.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

public abstract class IntervalAppliedStatusEffect extends StatusEffect {

    private final int updateInterval;

    public IntervalAppliedStatusEffect(int duration) {
        super(duration);
        this.updateInterval = this.getUpdateInterval();
    }

    public abstract int getUpdateInterval();

    public abstract void applyEffect(HealthContainer container, LivingEntity entity, @Nullable BodyPart limb);

    @Override
    public final void apply(HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
        Level level = entity.level();
        long gameTime = level.getGameTime();
        if (this.updateInterval <= 0 || gameTime % this.updateInterval == 0) {
            this.applyEffect(container, entity, limb);
        }
    }
}
