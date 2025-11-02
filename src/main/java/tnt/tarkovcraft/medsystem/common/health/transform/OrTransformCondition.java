package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTransformConditions;

import java.util.List;

public class OrTransformCondition implements TransformCondition {

    public static final MapCodec<OrTransformCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TransformConditionType.CODEC.listOf(2, 10).fieldOf("values").forGetter(t -> t.values)
    ).apply(instance, OrTransformCondition::new));

    private final List<TransformCondition> values;

    public OrTransformCondition(List<TransformCondition> values) {
        this.values = values;
    }

    @Override
    public boolean canApply(LivingEntity context) {
        for (TransformCondition condition : this.values) {
            if (condition.canApply(context))
                return true;
        }
        return false;
    }

    @Override
    public TransformConditionType<?> getType() {
        return MedSystemTransformConditions.OR.get();
    }
}
