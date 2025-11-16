package tnt.tarkovcraft.medsystem.common.health.math;

import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.Map;

public interface DamageDistributor {

    Map<Limb, Float> distribute(DamageContext context, HealthContainer container, float damage);
}
