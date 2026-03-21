package tnt.tarkovcraft.medsystem.common.effect.event.condition;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventParams;
import tnt.tarkovcraft.medsystem.common.effect.event.TriggerResult;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventConditions;

public final class LostLimbStatusEffectCondition implements StatusEffectEventCondition {

    public static final LostLimbStatusEffectCondition INSTANCE = new LostLimbStatusEffectCondition();
    public static final MapCodec<LostLimbStatusEffectCondition> CODEC = MapCodec.unit(INSTANCE);

    private LostLimbStatusEffectCondition() {}

    @Override
    public TriggerResult test(StatusEffectEventContext ctx) {
        int count = ctx.getParameterOrDefault(StatusEffectEventParams.LIMBS_LOST, 0);
        return count > 0 ? TriggerResult.SUCCESS : TriggerResult.FAILED;
    }

    @Override
    public StatusEffectEventConditionType<?> getType() {
        return MedSystemStatusEffectEventConditions.LOST_LIMB.value();
    }
}
