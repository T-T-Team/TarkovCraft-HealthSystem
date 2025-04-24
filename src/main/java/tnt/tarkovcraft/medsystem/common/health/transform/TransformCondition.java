package tnt.tarkovcraft.medsystem.common.health.transform;

import net.minecraft.world.entity.LivingEntity;

public interface TransformCondition {

    boolean canApply(LivingEntity context);

    TransformConditionType<?> getType();
}
