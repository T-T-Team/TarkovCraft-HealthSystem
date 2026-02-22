package tnt.tarkovcraft.medsystem.common.effect.event.condition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import tnt.tarkovcraft.core.common.data.number.ConstantNumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.TriggerResult;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventConditions;

import java.util.function.Function;

public record RandomChanceStatusEffectEventCondition(NumberProvider chance) implements StatusEffectEventCondition {

    public static final MapCodec<RandomChanceStatusEffectEventCondition> CODEC = Codec.either(NumberProviderType.ID_CODEC, Codec.FLOAT)
            .xmap(either -> new RandomChanceStatusEffectEventCondition(either.map(Function.identity(), ConstantNumberProvider::new)), condition -> Either.left(condition.chance))
            .fieldOf("chance");

    @Override
    public TriggerResult test(StatusEffectEventContext ctx) {
        RandomSource random = ctx.getEntity().getRandom();
        float chance = this.chance.floatValue();
        return TriggerResult.condition(random.nextFloat() < chance);
    }

    @Override
    public StatusEffectEventConditionType<?> getType() {
        return MedSystemStatusEffectEventConditions.RANDOM_CHANCE.value();
    }
}
