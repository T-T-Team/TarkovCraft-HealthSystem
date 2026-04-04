package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventConditions;

import java.util.List;

public record IsLimbTypeEventCondition(List<LimbType> limbs) implements HealthEventCondition {

    public static final MapCodec<IsLimbTypeEventCondition> CODEC = Codecs.list(LimbType.CODEC)
            .xmap(IsLimbTypeEventCondition::new, IsLimbTypeEventCondition::limbs).fieldOf("limb");

    @Override
    public HealthEventResult test(HealthEventContext ctx) {
        Limb limb = ctx.getLimb();
        return HealthEventResult.condition(this.limbs.contains(limb.getType()));
    }

    @Override
    public HealthEventConditionType<?> getType() {
        return MedSystemHealthEventConditions.IS_LIMB.value();
    }
}
