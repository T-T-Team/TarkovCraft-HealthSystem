package tnt.tarkovcraft.medsystem.common.effect.event.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.LogicalOperator;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.TriggerResult;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventConditions;

import java.util.List;

public record LogicalFunctionStatusEffectEventCondition(LogicalOperator operator, List<StatusEffectEventCondition> conditions) implements StatusEffectEventCondition {

    public static final MapCodec<LogicalFunctionStatusEffectEventCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LogicalOperator.CODEC.fieldOf("operator").forGetter(LogicalFunctionStatusEffectEventCondition::operator),
            StatusEffectEventConditionType.CODEC.listOf().fieldOf("conditions").forGetter(LogicalFunctionStatusEffectEventCondition::conditions)
    ).apply(instance, LogicalFunctionStatusEffectEventCondition::new));

    @Override
    public TriggerResult test(StatusEffectEventContext ctx) {
        return TriggerResult.condition(this.operator.apply(this.conditions, condition -> condition.test(ctx).isValid()));
    }

    @Override
    public StatusEffectEventConditionType<?> getType() {
        return MedSystemStatusEffectEventConditions.LOGICAL.value();
    }
}
