package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTags;

import java.util.ArrayList;
import java.util.List;

public final class MovementDamageHitCalculator implements HitCalculator {

    public static final MovementDamageHitCalculator INSTANCE = new MovementDamageHitCalculator();

    private MovementDamageHitCalculator() {
    }

    public static boolean canApply(HitCalculatorRule.Context context) {
        return context.source().is(MedSystemTags.DamageTypes.IS_MOVEMENT_RESTRICTED);
    }

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        List<HitResult> hits = new ArrayList<>();
        container.iterateHitboxes(
                entity,
                (hitbox, limb) -> HealthSystem.isMovementRestrictedOnLimb(limb),
                (hitbox, limb) -> hits.add(new HitResult(hitbox, limb))
        );
        return hits;
    }
}
