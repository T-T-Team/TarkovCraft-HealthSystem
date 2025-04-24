package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import tnt.tarkovcraft.medsystem.common.health.PositionedAABB;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHitboxTransforms;

public class ScaleHitboxTransform implements EntityHitboxTransform {

    public static final MapCodec<ScaleHitboxTransform> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Vec2.CODEC.fieldOf("scale").forGetter(t -> t.scale)
    ).apply(instance, ScaleHitboxTransform::new));

    private final Vec2 scale;

    public ScaleHitboxTransform(Vec2 scale) {
        this.scale = scale;
    }

    @Override
    public PositionedAABB apply(PositionedAABB current, LivingEntity context) {
        return current.scale(this.scale.x, this.scale.y);
    }

    @Override
    public EntityHitboxTransformType<?> getType() {
        return MedSystemHitboxTransforms.SCALE.get();
    }
}
