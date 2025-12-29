package tnt.tarkovcraft.medsystem.common.health.distributor;

import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.calc.HitResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PoisonDamageDistributor implements DamageDistributor {

    public static final PoisonDamageDistributor INSTANCE = new PoisonDamageDistributor();

    private PoisonDamageDistributor() {
    }

    @Override
    public Map<Limb, Float> distribute(DamageContext context, HealthContainer container, float damage) {
        int vitalLimbsCount = container.getVitalLimbs().size();
        float vitalDmgCutoff = 1.0F / vitalLimbsCount - 0.01F;
        List<Limb> limbs = context.getHits().stream().map(HitResult::limb)
                .filter(limb -> {
                    if (limb.isVital()) {
                        return limb.getHealth() > vitalDmgCutoff;
                    }
                    return limb.isAlive();
                })
                .toList();
        float perLimb = damage / limbs.size();
        Map<Limb, Float> damageMap = new HashMap<>();
        for (HitResult hit : context.getHits()) {
            Limb limb = hit.limb();
            float amount = Math.min(perLimb, limb.isVital() ? limb.getHealth() - vitalDmgCutoff : limb.getHealth());
            if (amount > 0)
                damageMap.put(limb, amount);
        }
        return damageMap;
    }
}
