package tnt.tarkovcraft.medsystem.common.effect.event.condition;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.TriggerResult;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventConditions;

public final class IsRootLimbStatusEffectEventCondition implements StatusEffectEventCondition {

    public static final IsRootLimbStatusEffectEventCondition INSTANCE = new IsRootLimbStatusEffectEventCondition();
    public static final MapCodec<IsRootLimbStatusEffectEventCondition> CODEC = MapCodec.unit(INSTANCE);

    private IsRootLimbStatusEffectEventCondition() {
    }

    @Override
    public TriggerResult test(StatusEffectEventContext ctx) {
        Limb limb = ctx.getLimb();
        HealthContainer container = ctx.getHealthContainer();
        return TriggerResult.condition(container.getRootLimbCode().equals(limb.getLimbCode()));
    }

    @Override
    public StatusEffectEventConditionType<?> getType() {
        return MedSystemStatusEffectEventConditions.IS_ROOT_LIMB.value();
    }
}
