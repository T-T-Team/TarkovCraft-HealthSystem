package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventConditions;

public final class IsRootLimbEventCondition implements HealthEventCondition {

    public static final IsRootLimbEventCondition INSTANCE = new IsRootLimbEventCondition();
    public static final MapCodec<IsRootLimbEventCondition> CODEC = MapCodec.unit(INSTANCE);

    private IsRootLimbEventCondition() {
    }

    @Override
    public HealthEventResult test(HealthEventContext ctx) {
        Limb limb = ctx.getLimb();
        HealthContainer container = ctx.getHealthContainer();
        return HealthEventResult.condition(container.getRootLimbCode().equals(limb.getLimbCode()));
    }

    @Override
    public HealthEventConditionType<?> getType() {
        return MedSystemHealthEventConditions.IS_ROOT_LIMB.value();
    }
}
