package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTransformConditions;

public class NotTransformCondition implements TransformCondition {

    public static final MapCodec<NotTransformCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TransformConditionType.CODEC.fieldOf("child").forGetter(t -> t.condition)
    ).apply(instance, NotTransformCondition::new));

    private final TransformCondition condition;

    public NotTransformCondition(TransformCondition condition) {
        this.condition = condition;
    }

    @Override
    public boolean canApply(LivingEntity context) {
        return !condition.canApply(context);
    }

    @Override
    public TransformConditionType<?> getType() {
        return MedSystemTransformConditions.NOT.get();
    }
}
