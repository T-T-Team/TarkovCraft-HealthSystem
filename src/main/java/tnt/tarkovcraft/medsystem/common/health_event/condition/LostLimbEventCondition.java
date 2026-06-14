package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventParams;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;

public final class LostLimbEventCondition implements HealthEventCondition {

    public static final LostLimbEventCondition INSTANCE = new LostLimbEventCondition();
    public static final MapCodec<LostLimbEventCondition> CODEC = MapCodec.unit(INSTANCE);

    private LostLimbEventCondition() {}

    @Override
    public HealthEventResult test(HealthEventContext ctx) {
        int count = ctx.getParameterOrDefault(HealthEventParams.LIMBS_LOST, 0);
        return count > 0 ? HealthEventResult.SUCCESS : HealthEventResult.FAILED;
    }

    @Override
    public MapCodec<? extends HealthEventCondition> codec() {
        return CODEC;
    }
}
