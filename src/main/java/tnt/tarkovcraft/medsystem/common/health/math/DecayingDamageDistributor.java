package tnt.tarkovcraft.medsystem.common.health.math;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HitResult;

import java.util.List;
import java.util.Map;

public class DecayingDamageDistributor implements DamageDistributor {

    private final float decayFactor;

    public DecayingDamageDistributor(float decayFactor) {
        this.decayFactor = decayFactor;
    }

    @Override
    public Map<BodyPart, Float> distribute(DamageContext context, HealthContainer container, float damage) {
        List<HitResult> hits = context.getHits();
        if (hits.size() == 1) {
            return Map.of(hits.getFirst().bodyPart(), damage);
        }
        BodyPart main = hits.getFirst().bodyPart();
        Object2FloatMap<BodyPart> map = new Object2FloatOpenHashMap<>();
        for (int i = 0; i < hits.size(); i++) {
            HitResult hit = hits.get(i);
            float partDamage = geometricDecay(damage, this.decayFactor, hits.size(), i);
            map.put(hit.bodyPart(), partDamage);
        }
        float damageSum = (float) map.values().doubleStream().sum();
        float extra = damage - damageSum;
        float mainDamage = map.getFloat(main);
        map.put(main, mainDamage + extra);
        return map;
    }

    public static float geometricDecay(float pool, float decayMult, int elements, int index) {
        float d = (1.0F - (float) Math.pow(decayMult, elements));
        float a = pool * (1.0F - decayMult) / d;
        return a * (float) Math.pow(decayMult, index);
    }
}
