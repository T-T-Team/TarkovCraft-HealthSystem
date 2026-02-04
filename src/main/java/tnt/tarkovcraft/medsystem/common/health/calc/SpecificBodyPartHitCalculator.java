package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.api.SpecificLimbDamage;
import tnt.tarkovcraft.medsystem.common.health.EntityHitboxContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthContainerDefinition;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.ArrayList;
import java.util.List;

public record SpecificBodyPartHitCalculator(SpecificLimbDamage damage) implements HitCalculator {

    public static boolean canApply(HitCalculationContext ctx) {
        return ctx.source() instanceof SpecificLimbDamage;
    }

    public static SpecificBodyPartHitCalculator createInstance(HitCalculationContext ctx) {
        SpecificLimbDamage source = (SpecificLimbDamage) ctx.source();
        return new SpecificBodyPartHitCalculator(source);
    }

    @Override
    public HitCalculationResult calculateHits(HitCalculationContext context) {
        HealthContainer container = context.container();
        LivingEntity entity = context.entity();
        HealthContainerDefinition definition = container.getDefinition();
        EntityHitboxContainer hitboxContainer = definition.hitboxContainer();
        String entityState = definition.getCurrentEntityState(entity);
        List<HitInfo> hits = new ArrayList<>();
        for (String limbCode : this.damage.getLimbs()) {
            if (container.hasLimb(limbCode)) {
                Limb limb = container.getLimbByCode(limbCode);
                if (!limb.isDead() || this.damage.canDamageDeadLimbs()) {
                    EntityHitboxContainer.LimbHitboxDefinition hitboxDefinition = hitboxContainer.getLimbHitbox(limbCode, entityState);
                    HitInfo result = new HitInfo(hitboxDefinition, limb, hitboxDefinition.toWorldSpaceHitbox(entity));
                    hits.add(result);
                }
            }
        }
        if (hits.isEmpty()) {
            return HitCalculationResult.simpleResult(context, hitbox -> this.damage.canDamageDeadLimbs() || !hitbox.limb().isDead());
        }
        return HitCalculationResult.of(hits);
    }
}
