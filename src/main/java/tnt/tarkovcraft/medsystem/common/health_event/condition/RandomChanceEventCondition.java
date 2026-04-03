package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;
import tnt.tarkovcraft.medsystem.common.health_event.function.HealthEventFunction;
import tnt.tarkovcraft.medsystem.common.health_event.function.HealthEventFunctionType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventConditions;

import java.util.Collections;
import java.util.List;

public record RandomChanceEventCondition(NumberProvider baseChance, List<HealthEventFunction> functions) implements HealthEventCondition {

    public static final MapCodec<RandomChanceEventCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberProviderType.VALUE_CODEC.fieldOf("base_chance").forGetter(RandomChanceEventCondition::baseChance),
            HealthEventFunctionType.CODEC.listOf().optionalFieldOf("functions", Collections.emptyList()).forGetter(RandomChanceEventCondition::functions)
    ).apply(instance, RandomChanceEventCondition::new));

    @Override
    public HealthEventResult test(HealthEventContext ctx) {
        RandomSource random = ctx.getEntity().getRandom();
        float baseChance = this.baseChance.floatValue();
        float chance = HealthEventFunctionType.applyFunctions(baseChance, ctx, this.functions);
        return HealthEventResult.condition(random.nextFloat() < chance);
    }

    @Override
    public HealthEventConditionType<?> getType() {
        return MedSystemHealthEventConditions.RANDOM_CHANCE.value();
    }
}
