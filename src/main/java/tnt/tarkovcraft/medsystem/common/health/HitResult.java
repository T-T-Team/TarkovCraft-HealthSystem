package tnt.tarkovcraft.medsystem.common.health;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record HitResult(BodyPartHitbox hitbox, BodyPart bodyPart, AABB aabb, Vec3 hit) {

    public HitResult(BodyPartHitbox hitbox, BodyPart bodyPart, AABB aabb) {
        this(hitbox, bodyPart, aabb, aabb.getCenter());
    }

    public HitResult(BodyPartHitbox hitbox, BodyPart bodyPart) {
        this(hitbox, bodyPart, null, null);
    }
}
