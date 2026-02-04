package tnt.tarkovcraft.medsystem.common.health.distributor;

import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.calc.HitInfo;

import java.util.HashMap;
import java.util.Map;

public final class FullDamageDistributor implements DamageDistributor {

    public static final FullDamageDistributor INSTANCE = new FullDamageDistributor();

    private FullDamageDistributor() {
    }

    @Override
    public Map<Limb, Float> distribute(DamageContext context, float damage) {
        Map<Limb, Float> damages = new HashMap<>();
        for (HitInfo hit : context.getHits()) {
            damages.put(hit.limb(), damage);
        }
        return damages;
    }
}
