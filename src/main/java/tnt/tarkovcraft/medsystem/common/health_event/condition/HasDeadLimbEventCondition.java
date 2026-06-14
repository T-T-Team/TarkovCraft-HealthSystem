package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.LimbContainer;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;

import java.util.Set;

public record HasDeadLimbEventCondition(Set<LimbType> limb) implements HealthEventCondition {

    public static final MapCodec<HasDeadLimbEventCondition> CODEC = Codecs.enumSet(LimbType.CODEC).fieldOf("limb")
            .xmap(HasDeadLimbEventCondition::new, HasDeadLimbEventCondition::limb);

    @Override
    public HealthEventResult test(HealthEventContext ctx) {
        HealthContainer container = ctx.getHealthContainer();
        LimbContainer limbContainer = container.getLimbContainer();
        return HealthEventResult.condition(limbContainer.hasLimb(limb -> limb.isDead() && this.limb.contains(limb.getType())));
    }

    @Override
    public MapCodec<? extends HealthEventCondition> codec() {
        return CODEC;
    }
}
