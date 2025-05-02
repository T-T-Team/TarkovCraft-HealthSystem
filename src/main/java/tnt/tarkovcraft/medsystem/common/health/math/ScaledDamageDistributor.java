package tnt.tarkovcraft.medsystem.common.health.math;

import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

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
    public Map<BodyPart, Float> distribute(DamageContext context, HealthContainer container, float damage) {
        Map<BodyPart, Float> damageMap = this.source.distribute(context, container, damage);
        Map<BodyPart, Float> result = new HashMap<>();
        for (Map.Entry<BodyPart, Float> entry : damageMap.entrySet()) {
            result.put(entry.getKey(), entry.getValue() * this.scale);
        }
        return result;
    }
}
