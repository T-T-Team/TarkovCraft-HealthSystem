package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.LogicalOperator;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventConditions;

import java.util.List;

public record LogicalEventCondition(LogicalOperator operator, List<HealthEventCondition> conditions) implements HealthEventCondition {

    public static final MapCodec<LogicalEventCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LogicalOperator.CODEC.fieldOf("operator").forGetter(LogicalEventCondition::operator),
            HealthEventConditionType.CODEC.listOf().fieldOf("conditions").forGetter(LogicalEventCondition::conditions)
    ).apply(instance, LogicalEventCondition::new));

    @Override
    public HealthEventResult test(HealthEventContext ctx) {
        return HealthEventResult.condition(this.operator.apply(this.conditions, condition -> condition.test(ctx).isValid()));
    }

    @Override
    public HealthEventConditionType<?> getType() {
        return MedSystemHealthEventConditions.LOGICAL.value();
    }
}
