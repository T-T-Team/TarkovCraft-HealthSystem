package tnt.tarkovcraft.medsystem.common.health.state;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.LogicalOperator;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStateFilters;

import java.util.List;

public record LogicalStateFilter(LogicalOperator operator, List<StateFilter> values) implements StateFilter {

    public static final MapCodec<LogicalStateFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LogicalOperator.CODEC.fieldOf("operator").forGetter(LogicalStateFilter::operator),
            StateFilterType.CODEC.listOf().fieldOf("values").forGetter(LogicalStateFilter::values)
    ).apply(instance, LogicalStateFilter::new));

    @Override
    public boolean matches(LivingEntity entity) {
        return this.operator.apply(this.values, filter -> filter.matches(entity));
    }

    @Override
    public StateFilterType<?> getType() {
        return MedSystemStateFilters.LOGICAL.value();
    }
}
