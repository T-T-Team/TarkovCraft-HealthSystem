package tnt.tarkovcraft.medsystem.common.effect.event.condition;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.TriggerResult;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventConditions;

public final class IsDeadLimbStatusEffectEventCondition implements StatusEffectEventCondition {

    public static final IsDeadLimbStatusEffectEventCondition INSTANCE = new IsDeadLimbStatusEffectEventCondition();
    public static final MapCodec<IsDeadLimbStatusEffectEventCondition> CODEC = MapCodec.unit(INSTANCE);

    private IsDeadLimbStatusEffectEventCondition() {}

    @Override
    public TriggerResult test(StatusEffectEventContext ctx) {
        return TriggerResult.condition(ctx.getLimb().isDead());
    }

    @Override
    public StatusEffectEventConditionType<?> getType() {
        return MedSystemStatusEffectEventConditions.IS_DEAD_LIMB.value();
    }
}
