package tnt.tarkovcraft.medsystem.common.health.state;

import net.minecraft.world.entity.LivingEntity;

public interface EntityStateMatcher {

    boolean matches(LivingEntity entity);

    EntityStateMatcherType<?> getType();
}
