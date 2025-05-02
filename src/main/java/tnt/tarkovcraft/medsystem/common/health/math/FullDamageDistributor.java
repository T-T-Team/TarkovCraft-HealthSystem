package tnt.tarkovcraft.medsystem.common.health.math;

import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HitResult;

import java.util.HashMap;
import java.util.Map;

public class FullDamageDistributor implements DamageDistributor {

    public static final FullDamageDistributor INSTANCE = new FullDamageDistributor();

    @Override
    public Map<BodyPart, Float> distribute(DamageContext context, HealthContainer container, float damage) {
        Map<BodyPart, Float> damages = new HashMap<>();
        for (HitResult hit : context.getHits()) {
            damages.put(hit.bodyPart(), damage);
        }
        return damages;
    }
}
