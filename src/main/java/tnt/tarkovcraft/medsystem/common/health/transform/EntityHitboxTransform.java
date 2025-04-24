package tnt.tarkovcraft.medsystem.common.health.transform;

import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.PositionedAABB;

public interface EntityHitboxTransform {

    PositionedAABB apply(PositionedAABB current, LivingEntity context);

    EntityHitboxTransformType<?> getType();
}
