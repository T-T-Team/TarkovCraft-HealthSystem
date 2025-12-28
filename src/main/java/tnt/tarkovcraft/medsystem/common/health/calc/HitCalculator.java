package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.distributor.DamageDistributor;

import java.util.List;

public interface HitCalculator {

    List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container);

    default DamageDistributor getCustomDamageDistributor(LivingEntity entity, DamageSource source, HealthContainer container, DamageDistributor original) {
        return null;
    }
}
