package tnt.tarkovcraft.medsystem.common.health.math;

import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HitResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EvenDamageDistributor implements DamageDistributor {

    public static final EvenDamageDistributor INSTANCE = new EvenDamageDistributor();

    private EvenDamageDistributor() {
    }

    @Override
    public Map<Limb, Float> distribute(DamageContext context, HealthContainer container, float damage) {
        List<HitResult> hits = context.getHits();
        float partDamage = damage / Math.max(1, hits.size());
        Map<Limb, Float> result = new HashMap<>();
        for (HitResult hit : hits) {
            result.put(hit.limb(), partDamage);
        }
        return result;
    }
}
