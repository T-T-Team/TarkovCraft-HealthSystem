package tnt.tarkovcraft.medsystem.common.damage_effect.condition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import tnt.tarkovcraft.core.common.data.number.ConstantNumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProvider;
import tnt.tarkovcraft.core.common.data.number.NumberProviderType;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectConditions;

import java.util.function.Function;

public record RandomChanceDamageEffectCondition(NumberProvider chance) implements DamageEffectCondition {

    public static final MapCodec<RandomChanceDamageEffectCondition> CODEC = Codec.either(NumberProviderType.ID_CODEC, Codec.FLOAT)
            .xmap(either -> new RandomChanceDamageEffectCondition(either.map(Function.identity(), ConstantNumberProvider::new)), condition -> Either.left(condition.chance))
            .fieldOf("chance");

    @Override
    public boolean matches(DamageEffectContext context) {
        RandomSource random = context.target().getRandom();
        float chance = this.chance.floatValue();
        return random.nextFloat() < chance;
    }

    @Override
    public DamageEffectConditionType<?> getType() {
        return MedSystemDamageEffectConditions.RANDOM_CHANCE.value();
    }
}
