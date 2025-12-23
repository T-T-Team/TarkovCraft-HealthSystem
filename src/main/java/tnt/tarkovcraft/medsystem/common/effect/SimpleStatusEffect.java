package tnt.tarkovcraft.medsystem.common.effect;

import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

public abstract class SimpleStatusEffect extends StatusEffect {

    public SimpleStatusEffect(int duration) {
        super(duration);
    }

    @Override
    public final void apply(HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
    }

    @Override
    public final void onRemoved(StatusEffectSubmitter submitter, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
    }
}
