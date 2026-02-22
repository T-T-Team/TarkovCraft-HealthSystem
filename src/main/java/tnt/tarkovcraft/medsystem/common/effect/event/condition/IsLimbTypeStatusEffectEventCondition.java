package tnt.tarkovcraft.medsystem.common.effect.event.condition;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.TriggerResult;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventConditions;

import java.util.List;

public record IsLimbTypeStatusEffectEventCondition(List<LimbType> limbs) implements StatusEffectEventCondition {

    public static final MapCodec<IsLimbTypeStatusEffectEventCondition> CODEC = Codecs.list(LimbType.CODEC)
            .xmap(IsLimbTypeStatusEffectEventCondition::new, IsLimbTypeStatusEffectEventCondition::limbs).fieldOf("limb");

    @Override
    public TriggerResult test(StatusEffectEventContext ctx) {
        Limb limb = ctx.getLimb();
        return TriggerResult.condition(this.limbs.contains(limb.getType()));
    }

    @Override
    public StatusEffectEventConditionType<?> getType() {
        return MedSystemStatusEffectEventConditions.IS_LIMB.value();
    }
}
