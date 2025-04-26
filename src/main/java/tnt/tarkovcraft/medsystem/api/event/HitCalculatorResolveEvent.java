package tnt.tarkovcraft.medsystem.api.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.math.HitCalculator;

public class HitCalculatorResolveEvent extends Event {

    private final LivingEntity entity;
    private final DamageSource damageSource;
    private final HealthContainer healthContainer;
    private HitCalculator calculator;

    public HitCalculatorResolveEvent(LivingEntity entity, DamageSource damageSource, HealthContainer healthContainer) {
        this.entity = entity;
        this.damageSource = damageSource;
        this.healthContainer = healthContainer;
    }

    public void setCalculator(HitCalculator calculator) {
        this.calculator = calculator;
    }

    public HitCalculator getCalculator() {
        return calculator;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public DamageSource getDamageSource() {
        return damageSource;
    }

    public HealthContainer getHealthContainer() {
        return healthContainer;
    }
}
