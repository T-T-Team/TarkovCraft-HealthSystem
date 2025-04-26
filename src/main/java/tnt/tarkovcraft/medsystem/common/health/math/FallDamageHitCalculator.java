package tnt.tarkovcraft.medsystem.common.health.math;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.BodyPartGroup;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HitResult;

import java.util.ArrayList;
import java.util.List;

public class FallDamageHitCalculator implements HitCalculator {

    public static final FallDamageHitCalculator INSTANCE = new FallDamageHitCalculator();

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        List<HitResult> results = new ArrayList<>();
        container.acceptHitboxes(
                (hitbox, part) -> part.getGroup() == BodyPartGroup.LEG,
                (hitbox, part) -> results.add(new HitResult(hitbox, part))
        );
        return results;
    }
}
