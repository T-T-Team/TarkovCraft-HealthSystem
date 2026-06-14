package tnt.tarkovcraft.medsystem.common.health.state;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.LogicalOperator;

import java.util.List;

public record LogicalEntityStateMatcher(LogicalOperator operator, List<EntityStateMatcher> values) implements EntityStateMatcher {

    public static final MapCodec<LogicalEntityStateMatcher> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LogicalOperator.CODEC.fieldOf("operator").forGetter(LogicalEntityStateMatcher::operator),
            EntityStateMatcher.CODEC.listOf().fieldOf("values").forGetter(LogicalEntityStateMatcher::values)
    ).apply(instance, LogicalEntityStateMatcher::new));

    @Override
    public boolean matches(LivingEntity entity) {
        return this.operator.apply(this.values, stateMatcher -> stateMatcher.matches(entity));
    }

    @Override
    public MapCodec<? extends EntityStateMatcher> codec() {
        return CODEC;
    }
}
