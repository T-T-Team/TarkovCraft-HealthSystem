package tnt.tarkovcraft.medsystem.common.health.math;

import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

import java.util.Map;

public interface DamageDistributor {

    Map<BodyPart, Float> distribute(DamageContext context, HealthContainer container, float damage);
}
