package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTransformConditions;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

public class UnconsciousTransformCondition implements TransformCondition {

    public static final UnconsciousTransformCondition INSTANCE = new UnconsciousTransformCondition();
    public static final MapCodec<UnconsciousTransformCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean canApply(LivingEntity context) {
        return !context.isPassenger() && BloodSystem.isEntityUnconscious(context);
    }

    @Override
    public TransformConditionType<?> getType() {
        return MedSystemTransformConditions.UNCONSCIOUS.get();
    }
}
