package tnt.tarkovcraft.medsystem.common.health.distributor;

import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.HashMap;
import java.util.Map;

public class ScaledDamageDistributor implements DamageDistributor {

    private final float scale;
    private final DamageDistributor source;

    public ScaledDamageDistributor(float scale, DamageDistributor source) {
        this.scale = scale;
        this.source = source;
    }

    @Override
    public Map<Limb, Float> distribute(DamageContext context, HealthContainer container, float damage) {
        Map<Limb, Float> damageMap = this.source.distribute(context, container, damage);
        Map<Limb, Float> result = new HashMap<>();
        for (Map.Entry<Limb, Float> entry : damageMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue() * this.scale);
        }
        return result;
    }
}
