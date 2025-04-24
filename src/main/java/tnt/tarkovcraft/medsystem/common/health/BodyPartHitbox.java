package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.health.transform.HitboxTransformDefinition;

import java.util.Collections;
import java.util.List;

public final class BodyPartHitbox {

    public static final Codec<BodyPartHitbox> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            Codec.STRING.fieldOf("owner").forGetter(BodyPartHitbox::getOwner),
            Codecs.list(HitboxTransformDefinition.CODEC).optionalFieldOf("transforms",Collections.emptyList()).forGetter(t -> t.transforms),
            Vec3.CODEC.fieldOf("pos").forGetter(t -> t.aabb.center()),
            Vec2.CODEC.fieldOf("size").forGetter(t -> t.aabb.size())
    ).apply(instance, BodyPartHitbox::new));

    private final String owner;
    private final List<HitboxTransformDefinition> transforms;
    private final PositionedAABB aabb;

    private BodyPartHitbox(String owner, List<HitboxTransformDefinition> transforms, Vec3 pos, Vec2 size) {
        this.owner = owner;
        this.transforms = transforms;
        this.aabb = PositionedAABB.create(pos, size);
    }

    public PositionedAABB transform(LivingEntity ctx) {
        PositionedAABB positionedAABB = this.aabb;
        for (HitboxTransformDefinition transform : this.transforms) {
            positionedAABB = transform.apply(positionedAABB, ctx);
        }
        return positionedAABB;
    }

    public AABB getLevelPositionedAABB(LivingEntity ctx) {
        return this.transform(ctx).move(ctx.position()).aabb();
    }

    public String getOwner() {
        return owner;
    }
}