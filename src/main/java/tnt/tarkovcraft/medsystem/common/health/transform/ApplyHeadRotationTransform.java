package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.PositionedAABB;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHitboxTransforms;

public class ApplyHeadRotationTransform implements EntityHitboxTransform {

    public static final ApplyHeadRotationTransform INSTANCE = new ApplyHeadRotationTransform();
    public static final MapCodec<ApplyHeadRotationTransform> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public PositionedAABB apply(PositionedAABB current, LivingEntity context) {
        float pitch = context.getXRot();
        float yaw = context.getYHeadRot();
        return current
                .pivotRotateX(-pitch * (float) Math.PI / 180.0F, current.center().subtract(0.0, current.height() / 2, 0.0))
                .rotateY(-yaw * (float) Math.PI / 180.0F);
    }

    @Override
    public EntityHitboxTransformType<?> getType() {
        return MedSystemHitboxTransforms.ENTITY_HEAD_ROTATION.get();
    }
}
