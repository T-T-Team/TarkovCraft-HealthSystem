package tnt.tarkovcraft.medsystem.common.health.math;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HitResult;

import java.util.List;

public record DelegateHitCalculator(HitCalculator delegate, DamageDistributor distributor) implements HitCalculator {

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        return this.delegate.calculateHits(entity, source, container);
    }

    @Override
    public DamageDistributor getCustomDamageDistributor(LivingEntity entity, DamageSource source, HealthContainer container, DamageDistributor original) {
        return this.distributor;
    }
}
