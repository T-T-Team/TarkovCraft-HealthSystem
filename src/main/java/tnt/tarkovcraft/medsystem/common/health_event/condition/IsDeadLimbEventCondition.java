package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventConditions;

public final class IsDeadLimbEventCondition implements HealthEventCondition {

    public static final IsDeadLimbEventCondition INSTANCE = new IsDeadLimbEventCondition();
    public static final MapCodec<IsDeadLimbEventCondition> CODEC = MapCodec.unit(INSTANCE);

    private IsDeadLimbEventCondition() {}

    @Override
    public HealthEventResult test(HealthEventContext ctx) {
        return HealthEventResult.condition(ctx.getLimb().isDead());
    }

    @Override
    public HealthEventConditionType<?> getType() {
        return MedSystemHealthEventConditions.IS_DEAD_LIMB.value();
    }
}
