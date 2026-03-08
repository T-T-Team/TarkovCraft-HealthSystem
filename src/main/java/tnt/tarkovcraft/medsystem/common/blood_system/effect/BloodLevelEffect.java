package tnt.tarkovcraft.medsystem.common.blood_system.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;

public interface BloodLevelEffect {

    void applyEffects(LivingEntity entity, ServerLevel level, EntityBloodSystem bloodSystem);

    BloodLevelEffectType<?> getType();
}
