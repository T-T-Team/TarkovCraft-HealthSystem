package tnt.tarkovcraft.medsystem.common.health;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public record HitResult(BodyPartHitbox hitbox, Limb limb, AABB aabb, Vec3 hit) {

    public HitResult(BodyPartHitbox hitbox, Limb limb, AABB aabb) {
        this(hitbox, limb, aabb, aabb.getCenter());
    }

    public HitResult(BodyPartHitbox hitbox, Limb limb) {
        this(hitbox, limb, null, null);
    }
}
