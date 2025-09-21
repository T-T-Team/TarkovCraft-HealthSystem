package tnt.tarkovcraft.medsystem.common.effect;

import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

import javax.annotation.Nullable;

public abstract class SimpleStatusEffect extends StatusEffect {

    public SimpleStatusEffect(int duration) {
        super(duration);
    }

    @Override
    public final void apply(HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
    }

    @Override
    public final void onRemoved(StatusEffectSubmitter submitter, HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
    }
}
