package tnt.tarkovcraft.medsystem.common.health.distributor;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.calc.HitInfo;

import java.util.List;
import java.util.Map;

public record DecayingDamageDistributor(float decayFactor) implements DamageDistributor {

    @Override
    public Map<Limb, Float> distribute(DamageContext context, float damage) {
        List<HitInfo> hits = context.getHits();
        if (hits.size() == 1) {
            return Map.of(hits.getFirst().limb(), damage);
        }
        Limb main = hits.getFirst().limb();
        Object2FloatMap<Limb> map = new Object2FloatOpenHashMap<>();
        for (int i = 0; i < hits.size(); i++) {
            HitInfo hit = hits.get(i);
            float partDamage = geometricDecay(damage, this.decayFactor, hits.size(), i);
            map.put(hit.limb(), partDamage);
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
