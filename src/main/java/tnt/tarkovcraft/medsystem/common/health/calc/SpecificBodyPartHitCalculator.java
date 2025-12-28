package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.api.SpecificLimbDamage;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.ArrayList;
import java.util.List;

public record SpecificBodyPartHitCalculator(SpecificLimbDamage damage) implements HitCalculator {

    public static boolean canApply(HitCalculatorRule.Context ctx) {
        return ctx.source() instanceof SpecificLimbDamage;
    }

    public static SpecificBodyPartHitCalculator createInstance(HitCalculatorRule.Context ctx) {
        SpecificLimbDamage source = (SpecificLimbDamage) ctx.source();
        return new SpecificBodyPartHitCalculator(source);
    }

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        List<HitResult> hits = new ArrayList<>();
        for (String limbCode : this.damage.getLimbs()) {
            if (container.hasLimb(limbCode)) {
                Limb part = container.getLimbByCode(limbCode);
                if (!part.isDead() || this.damage.canDamageDeadLimbs()) {
                    HitResult result = new HitResult(null, container.getLimbByCode(limbCode));
                    hits.add(result);
                }

            }
        }
        if (hits.isEmpty()) {
            container.iterateHitboxes(
                    entity,
                    (hitbox, limb) -> this.damage.canDamageDeadLimbs() || !limb.isDead(),
                    (hitbox, limb) -> hits.add(new HitResult(hitbox, limb))
            );
        }
        return hits;
    }
}
