package tnt.tarkovcraft.medsystem.common.health.reaction.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.reaction.HealthEventSource;

import javax.annotation.Nullable;

public interface HealthSourceEvent {

    void onReactionPassed(HealthEventSource source, HealthContainer container, LivingEntity entity, @Nullable DamageSource damageSource, BodyPart limb);

    HealthSourceEventType<?> getType();
}
