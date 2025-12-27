package tnt.tarkovcraft.medsystem.common.health.state;

import net.minecraft.world.entity.LivingEntity;

public interface StateFilter {

    boolean matches(LivingEntity entity);

    StateFilterType<?> getType();
}
