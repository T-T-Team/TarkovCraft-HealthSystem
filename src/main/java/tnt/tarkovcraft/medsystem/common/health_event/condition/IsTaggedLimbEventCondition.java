package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;

public record IsTaggedLimbEventCondition(Identifier tag) implements HealthEventCondition {

    public static final MapCodec<IsTaggedLimbEventCondition> CODEC = Identifier.CODEC
            .xmap(IsTaggedLimbEventCondition::new, IsTaggedLimbEventCondition::tag).fieldOf("tag");

    @Override
    public HealthEventResult test(HealthEventContext ctx) {
        Limb limb = ctx.getLimb();
        return HealthEventResult.condition(limb.isTagged(this.tag));
    }

    @Override
    public MapCodec<? extends HealthEventCondition> codec() {
        return CODEC;
    }
}
