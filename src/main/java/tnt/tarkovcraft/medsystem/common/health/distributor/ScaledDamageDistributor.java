package tnt.tarkovcraft.medsystem.common.health.distributor;

import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.HashMap;
import java.util.Map;

public record ScaledDamageDistributor(float scale, DamageDistributor source) implements DamageDistributor {

    public ScaledDamageDistributor(float scale) {
        this(scale, EvenDamageDistributor.INSTANCE);
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
