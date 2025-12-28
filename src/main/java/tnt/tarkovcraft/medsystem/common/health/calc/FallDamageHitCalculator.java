package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.health.distributor.DamageDistributor;
import tnt.tarkovcraft.medsystem.common.health.distributor.ScaledDamageDistributor;

import java.util.ArrayList;
import java.util.List;

public final class FallDamageHitCalculator implements HitCalculator {

    public static final FallDamageHitCalculator INSTANCE = new FallDamageHitCalculator();

    private FallDamageHitCalculator() {
    }

    public static boolean isFall(HitCalculatorRule.Context context) {
        return context.source().is(DamageTypeTags.IS_FALL);
    }

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        List<HitResult> results = new ArrayList<>();
        container.iterateHitboxes(
                entity,
                (hitbox, limb) -> limb.getType() == LimbType.LEG,
                (hitbox, limb) -> results.add(new HitResult(hitbox, limb))
        );
        return results;
    }

    @Override
    public DamageDistributor getCustomDamageDistributor(LivingEntity entity, DamageSource source, HealthContainer container, DamageDistributor original) {
        return new ScaledDamageDistributor(1.7F, original);
    }
}
