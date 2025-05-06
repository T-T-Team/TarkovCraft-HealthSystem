package tnt.tarkovcraft.medsystem.common.health.math;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.HitResult;

import java.util.ArrayList;
import java.util.List;

public class MovementDamageHitCalculator implements HitCalculator {

    public static final MovementDamageHitCalculator INSTANCE = new MovementDamageHitCalculator();

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        List<HitResult> hits = new ArrayList<>();
        container.acceptHitboxes(
                (hitbox, part) -> HealthSystem.isMovementRestrictingPart(part),
                (hitbox, part) -> hits.add(new HitResult(hitbox, part))
        );
        return hits;
    }
}
