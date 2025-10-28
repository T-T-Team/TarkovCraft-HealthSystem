package tnt.tarkovcraft.medsystem.common.health.reaction.function;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

import javax.annotation.Nullable;

public interface ChanceFunction {

    float apply(float chance, HealthContainer container, LivingEntity entity, @Nullable DamageSource source, Limb limb);

    ChanceFunctionType<?> getType();
}
