package tnt.tarkovcraft.medsystem.common.damage_effect;

import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.List;
import java.util.Map;

public interface DamageEffectContext {

    DamageEffectContextType contextType();

    LivingEntity target();

    Limb limb();

    HealthContainer health();

    DamageContext damageContext();

    float totalDamage();

    float limbDamage();

    Map<Limb, Float> damageDistribution();

    List<Limb> lostLimbs();

    default float getDamage(boolean localDamage) {
        return localDamage ? limbDamage() : totalDamage();
    }

    record UpdateDamageEffectContext(LivingEntity target, HealthContainer health, Limb limb) implements DamageEffectContext {

        @Override
        public DamageEffectContextType contextType() {
            return DamageEffectContextType.ON_UPDATE;
        }

        @Override
        public DamageContext damageContext() {
            throw new UnsupportedOperationException("Function not available in update context");
        }

        @Override
        public float totalDamage() {
            throw new UnsupportedOperationException("Function not available in update context");
        }

        @Override
        public float limbDamage() {
            throw new UnsupportedOperationException("Function not available in update context");
        }

        @Override
        public Map<Limb, Float> damageDistribution() {
            throw new UnsupportedOperationException("Function not available in update context");
        }

        @Override
        public List<Limb> lostLimbs() {
            throw new UnsupportedOperationException("Function not available in update context");
        }
    }

    record ApplyDamageEffectContext(LivingEntity target, HealthContainer health, Limb limb, DamageContext damageContext, float totalDamage, float limbDamage, Map<Limb, Float> damageDistribution, List<Limb> lostLimbs) implements DamageEffectContext {

        @Override
        public DamageEffectContextType contextType() {
            return DamageEffectContextType.ON_HURT;
        }
    }
}
