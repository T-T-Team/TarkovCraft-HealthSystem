package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

import java.util.ArrayList;
import java.util.List;

public class GenericHitCalculator implements HitCalculator {

    public static final GenericHitCalculator INSTANCE = new GenericHitCalculator();

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        List<HitResult> result = new ArrayList<>();
        container.iterateHitboxes(
                entity,
                (hitbox, part) -> result.add(new HitResult(hitbox, part))
        );
        return result;
    }
}
