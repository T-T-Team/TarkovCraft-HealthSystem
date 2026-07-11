package tnt.tarkovcraft.medsystem.common.damage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.damage.condition.DamageCondition;
import tnt.tarkovcraft.medsystem.common.damage.function.DamageFunction;
import tnt.tarkovcraft.medsystem.common.damage.function.GenericDamageFunction;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationResult;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculator;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public record DamageResolver(int priority, List<DamageCondition> conditions, DamageFunction calculator) implements Predicate<HitCalculationContext>, Comparable<DamageResolver> {

    public static final Codec<DamageResolver> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("priority", 0).forGetter(DamageResolver::priority),
            DamageCondition.CODEC.listOf().optionalFieldOf("conditions", Collections.emptyList()).forGetter(DamageResolver::conditions),
            DamageFunction.CODEC.fieldOf("calculator").forGetter(DamageResolver::calculator)
    ).apply(instance, DamageResolver::new));

    public static DamageResolver create(int priority, DamageFunction function, DamageCondition... conditions) {
        return new DamageResolver(priority, List.of(conditions), function);
    }

    public static DamageResolver generic() {
        return create(0, new GenericDamageFunction());
    }

    @Override
    public boolean test(HitCalculationContext context) {
        for (DamageCondition condition : this.conditions) {
            if (!condition.test(context)) {
                return false;
            }
        }
        return true;
    }

    public HitCalculationResult calculate(HitCalculationContext context) {
        HitCalculator hitCalculator = this.calculator.resolve(context);
        return hitCalculator.calculateHits(context);
    }

    @Override
    public int compareTo(@Nonnull DamageResolver o) {
        return Integer.compare(priority, o.priority);
    }
}
