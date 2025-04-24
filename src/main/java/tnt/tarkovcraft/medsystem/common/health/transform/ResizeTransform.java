package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import tnt.tarkovcraft.medsystem.common.health.PositionedAABB;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHitboxTransforms;

public class ResizeTransform implements EntityHitboxTransform {

    public static final MapCodec<ResizeTransform> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Vec2.CODEC.fieldOf("size").forGetter(t -> t.size)
    ).apply(instance, ResizeTransform::new));

    private final Vec2 size;

    public ResizeTransform(Vec2 size) {
        this.size = size;
    }

    @Override
    public PositionedAABB apply(PositionedAABB current, LivingEntity context) {
        return current.resize(this.size);
    }

    @Override
    public EntityHitboxTransformType<?> getType() {
        return MedSystemHitboxTransforms.RESIZE.get();
    }
}
