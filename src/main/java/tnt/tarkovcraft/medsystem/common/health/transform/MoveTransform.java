package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.PositionedAABB;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHitboxTransforms;

public class MoveTransform implements EntityHitboxTransform {

    public static final MapCodec<MoveTransform> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Vec3.CODEC.fieldOf("pos").forGetter(t -> t.pos)
    ).apply(instance, MoveTransform::new));

    private final Vec3 pos;

    public MoveTransform(Vec3 pos) {
        this.pos = pos;
    }

    @Override
    public PositionedAABB apply(PositionedAABB current, LivingEntity context) {
        return current.move(this.pos);
    }

    @Override
    public EntityHitboxTransformType<?> getType() {
        return MedSystemHitboxTransforms.MOVE.get();
    }
}
