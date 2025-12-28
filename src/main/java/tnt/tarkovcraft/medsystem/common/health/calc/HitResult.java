package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.EntityHitboxContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

public record HitResult(EntityHitboxContainer.LimbHitbox hitbox, Limb limb, AABB aabb, Vec3 hit) {

    public HitResult(EntityHitboxContainer.LimbHitbox hitbox, Limb limb, AABB aabb) {
        this(hitbox, limb, aabb, aabb.getCenter());
    }

    public HitResult(EntityHitboxContainer.LimbHitbox hitbox, Limb limb) {
        this(hitbox, limb, null, null);
    }
}
