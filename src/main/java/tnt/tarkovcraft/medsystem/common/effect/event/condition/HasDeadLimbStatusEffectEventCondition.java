package tnt.tarkovcraft.medsystem.common.effect.event.condition;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.TriggerResult;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.LimbContainer;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventConditions;

import java.util.Set;

public record HasDeadLimbStatusEffectEventCondition(Set<LimbType> limb) implements StatusEffectEventCondition {

    public static final MapCodec<HasDeadLimbStatusEffectEventCondition> CODEC = Codecs.enumSet(LimbType.CODEC).fieldOf("limb")
            .xmap(HasDeadLimbStatusEffectEventCondition::new, HasDeadLimbStatusEffectEventCondition::limb);

    @Override
    public TriggerResult test(StatusEffectEventContext ctx) {
        HealthContainer container = ctx.getHealthContainer();
        LimbContainer limbContainer = container.getLimbContainer();
        return TriggerResult.condition(limbContainer.hasLimb(limb -> limb.isDead() && this.limb.contains(limb.getType())));
    }

    @Override
    public StatusEffectEventConditionType<?> getType() {
        return MedSystemStatusEffectEventConditions.HAS_DEAD_LIMB.value();
    }
}
