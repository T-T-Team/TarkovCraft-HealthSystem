package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.api.SpecificLimbDamage;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.ArrayList;
import java.util.List;

public class SpecificBodyPartHitCalculator implements HitCalculator {

    private final String[] bodyParts;
    private final boolean allowDeadBodyParts;

    public SpecificBodyPartHitCalculator(String[] bodyParts, boolean allowDeadBodyParts) {
        this.bodyParts = bodyParts;
        this.allowDeadBodyParts = allowDeadBodyParts;
    }

    public static boolean canApply(HitCalculatorRule.Context ctx) {
        return ctx.source() instanceof SpecificLimbDamage;
    }

    public static SpecificBodyPartHitCalculator createInstance(HitCalculatorRule.Context ctx) {
        SpecificLimbDamage source = (SpecificLimbDamage) ctx.source();
        return new SpecificBodyPartHitCalculator(source.getLimbs(), source.canDamageDeadLimbs());
    }

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        List<HitResult> hits = new ArrayList<>();
        for (String bodyPartId : this.bodyParts) {
            if (container.hasLimb(bodyPartId)) {
                Limb part = container.getLimbByCode(bodyPartId);
                if (!part.isDead() || this.allowDeadBodyParts) {
                    HitResult result = new HitResult(null, container.getLimbByCode(bodyPartId));
                    hits.add(result);
                }

            }
        }
        if (hits.isEmpty()) {
            container.iterateHitboxes(
                    entity,
                    (hitbox, limb) -> this.allowDeadBodyParts || !limb.isDead(),
                    (hitbox, limb) -> hits.add(new HitResult(hitbox, limb))
            );
        }
        return hits;
    }
}
