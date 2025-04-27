package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTransformConditions;

public class IsBabyTransformCondition implements TransformCondition {

    public static final IsBabyTransformCondition INSTANCE = new IsBabyTransformCondition();
    public static final MapCodec<IsBabyTransformCondition> CODEC = MapCodec.unit(INSTANCE);

    private IsBabyTransformCondition() {
    }

    @Override
    public boolean canApply(LivingEntity context) {
        return context.isBaby();
    }

    @Override
    public TransformConditionType<?> getType() {
        return MedSystemTransformConditions.IS_BABY.get();
    }
}
