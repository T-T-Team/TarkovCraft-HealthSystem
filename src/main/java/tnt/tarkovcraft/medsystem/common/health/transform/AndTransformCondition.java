package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTransformConditions;

import java.util.List;

public class AndTransformCondition implements TransformCondition {

    public static final MapCodec<AndTransformCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TransformConditionType.CODEC.listOf(2, 10).fieldOf("values").forGetter(t -> t.values)
    ).apply(instance, AndTransformCondition::new));

    private final List<TransformCondition> values;

    public AndTransformCondition(List<TransformCondition> values) {
        this.values = values;
    }

    @Override
    public boolean canApply(LivingEntity context) {
        for (TransformCondition condition : this.values) {
            if (!condition.canApply(context))
                return false;
        }
        return true;
    }

    @Override
    public TransformConditionType<?> getType() {
        return MedSystemTransformConditions.AND.get();
    }
}
