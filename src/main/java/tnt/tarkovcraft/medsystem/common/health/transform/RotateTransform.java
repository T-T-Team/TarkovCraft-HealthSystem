package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.PositionedAABB;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHitboxTransforms;

public class RotateTransform implements EntityHitboxTransform {

    public static final MapCodec<RotateTransform> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.fieldOf("yaw").forGetter(t -> t.yaw)
    ).apply(instance, RotateTransform::new));

    private final float yaw;

    public RotateTransform(float yaw) {
        this.yaw = yaw;
    }

    @Override
    public PositionedAABB apply(PositionedAABB current, LivingEntity context) {
        return current.rotateY((float) Math.toRadians(this.yaw));
    }

    @Override
    public EntityHitboxTransformType<?> getType() {
        return MedSystemHitboxTransforms.ROTATE.get();
    }
}
