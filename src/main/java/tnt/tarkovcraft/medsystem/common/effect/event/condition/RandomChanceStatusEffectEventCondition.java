package tnt.tarkovcraft.medsystem.common.effect.event.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.TriggerResult;
import tnt.tarkovcraft.medsystem.common.effect.event.function.StatusEffectEventFunction;
import tnt.tarkovcraft.medsystem.common.effect.event.function.StatusEffectEventFunctionType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventConditions;

import java.util.Collections;
import java.util.List;

public record RandomChanceStatusEffectEventCondition(NumberProvider baseChance, List<StatusEffectEventFunction> functions) implements StatusEffectEventCondition {

    public static final MapCodec<RandomChanceStatusEffectEventCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberProviderType.VALUE_CODEC.fieldOf("base_chance").forGetter(RandomChanceStatusEffectEventCondition::baseChance),
            StatusEffectEventFunctionType.CODEC.listOf().optionalFieldOf("functions", Collections.emptyList()).forGetter(RandomChanceStatusEffectEventCondition::functions)
    ).apply(instance, RandomChanceStatusEffectEventCondition::new));

    @Override
    public TriggerResult test(StatusEffectEventContext ctx) {
        RandomSource random = ctx.getEntity().getRandom();
        float baseChance = this.baseChance.floatValue();
        float chance = StatusEffectEventFunctionType.applyFunctions(baseChance, ctx, this.functions);
        return TriggerResult.condition(random.nextFloat() < chance);
    }

    @Override
    public StatusEffectEventConditionType<?> getType() {
        return MedSystemStatusEffectEventConditions.RANDOM_CHANCE.value();
    }
}
