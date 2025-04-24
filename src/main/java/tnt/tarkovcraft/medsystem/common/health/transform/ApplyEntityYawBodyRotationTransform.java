package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.PositionedAABB;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHitboxTransforms;

public class ApplyEntityYawBodyRotationTransform implements EntityHitboxTransform {

    public static final ApplyEntityYawBodyRotationTransform INSTANCE = new ApplyEntityYawBodyRotationTransform();
    public static final MapCodec<ApplyEntityYawBodyRotationTransform> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public PositionedAABB apply(PositionedAABB current, LivingEntity context) {
        float rotation = context.yBodyRot;
        return current.rotateY(-rotation * (float) Math.PI / 180.0F);
    }

    @Override
    public EntityHitboxTransformType<?> getType() {
        return MedSystemHitboxTransforms.ENTITY_BODY_YAW.get();
    }
}
