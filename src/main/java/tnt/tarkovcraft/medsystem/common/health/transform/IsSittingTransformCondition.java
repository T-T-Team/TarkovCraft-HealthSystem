package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTransformConditions;

public class IsSittingTransformCondition implements TransformCondition {

    public static final IsSittingTransformCondition INSTANCE = new IsSittingTransformCondition();
    public static final MapCodec<IsSittingTransformCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean canApply(LivingEntity context) {
        return context.getVehicle() != null && context.getVehicle().shouldRiderSit();
    }

    @Override
    public TransformConditionType<?> getType() {
        return MedSystemTransformConditions.IS_SITTING.get();
    }
}
