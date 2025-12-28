package tnt.tarkovcraft.medsystem.common.health.distributor;

import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.calc.HitResult;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.List;
import java.util.Map;

public class DecayingDamageDistributor implements DamageDistributor {

    public static final DecayingDamageDistributor PROJECTILE = new DecayingDamageDistributor(0.5F);

    private final float decayFactor;

    public DecayingDamageDistributor(float decayFactor) {
        this.decayFactor = decayFactor;
    }

    @Override
    public Map<Limb, Float> distribute(DamageContext context, HealthContainer container, float damage) {
        List<HitResult> hits = context.getHits();
        if (hits.size() == 1) {
            return Map.of(hits.getFirst().limb(), damage);
        }
        Limb main = hits.getFirst().limb();
        Object2FloatMap<Limb> map = new Object2FloatOpenHashMap<>();
        for (int i = 0; i < hits.size(); i++) {
            HitResult hit = hits.get(i);
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
