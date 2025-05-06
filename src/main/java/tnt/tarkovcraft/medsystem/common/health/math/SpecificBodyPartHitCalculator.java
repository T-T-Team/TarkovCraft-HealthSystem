package tnt.tarkovcraft.medsystem.common.health.math;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HitResult;

import java.util.ArrayList;
import java.util.List;

public class SpecificBodyPartHitCalculator implements HitCalculator {

    private final String[] bodyParts;
    private final boolean allowDeadBodyParts;

    public SpecificBodyPartHitCalculator(String[] bodyParts, boolean allowDeadBodyParts) {
        this.bodyParts = bodyParts;
        this.allowDeadBodyParts = allowDeadBodyParts;
    }

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        List<HitResult> hits = new ArrayList<>();
        for (String bodyPartId : this.bodyParts) {
            if (container.hasBodyPart(bodyPartId)) {
                BodyPart part = container.getBodyPart(bodyPartId);
                if (!part.isDead() || this.allowDeadBodyParts) {
                    HitResult result = new HitResult(null, container.getBodyPart(bodyPartId));
                    hits.add(result);
                }

            }
        }
        if (hits.isEmpty()) {
            container.acceptHitboxes(
                    (hitbox, part) -> this.allowDeadBodyParts || !part.isDead(),
                    (hitbox, part) -> hits.add(new HitResult(hitbox, part))
            );
        }
        return hits;
    }
}
