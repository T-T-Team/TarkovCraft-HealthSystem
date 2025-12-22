package tnt.tarkovcraft.medsystem.common.damage_effect.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.LogicalOperator;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContextType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectConditions;

import java.util.List;

public record LogicalFunctionDamageEffectCondition(LogicalOperator operator, List<DamageEffectCondition> conditions) implements DamageEffectCondition {

    public static final MapCodec<LogicalFunctionDamageEffectCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LogicalOperator.CODEC.fieldOf("operator").forGetter(LogicalFunctionDamageEffectCondition::operator),
            DamageEffectConditionType.CODEC.listOf().fieldOf("conditions").forGetter(LogicalFunctionDamageEffectCondition::conditions)
    ).apply(instance, LogicalFunctionDamageEffectCondition::new));

    @Override
    public boolean matches(DamageEffectContext context) {
        return this.operator.apply(this.conditions, condition -> condition.matches(context));
    }

    @Override
    public DamageEffectConditionType<?> getType() {
        return MedSystemDamageEffectConditions.LOGICAL.value();
    }

    @Override
    public void validate(DamageEffectContextType contextType) {
        this.conditions.forEach(condition -> condition.validate(contextType));
    }
}
