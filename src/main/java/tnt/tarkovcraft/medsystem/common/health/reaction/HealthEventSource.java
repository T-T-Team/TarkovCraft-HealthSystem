package tnt.tarkovcraft.medsystem.common.health.reaction;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

import javax.annotation.Nullable;

public interface HealthEventSource {

    boolean canReact(HealthContainer container, LivingEntity entity, @Nullable DamageSource damageSource, BodyPart limb);

    HealthEventSourceType<?> getType();
}
