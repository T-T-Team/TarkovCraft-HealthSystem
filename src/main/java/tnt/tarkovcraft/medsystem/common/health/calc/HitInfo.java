package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.EntityHitboxContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.Objects;

public record HitInfo(EntityHitboxContainer.LimbHitboxDefinition hitbox, Limb limb, AABB aabb, Vec3 entryPoint) {

    public HitInfo(EntityHitboxContainer.LimbHitboxDefinition hitbox, Limb limb, AABB aabb) {
        this(hitbox, limb, aabb, aabb.getCenter());
    }

    public static HitInfo create(LimbHitbox hitbox, AABB aabb, Vec3 hit) {
        return new HitInfo(hitbox.definition(), hitbox.limb(), aabb, hit);
    }

    public static HitInfo create(LimbHitbox hitbox, AABB aabb) {
        return new HitInfo(hitbox.definition(), hitbox.limb(), aabb, aabb.getCenter());
    }

    public static HitInfo create(LimbHitbox hitbox, LivingEntity entity) {
        EntityHitboxContainer.LimbHitboxDefinition definition = hitbox.definition();
        AABB aabb = definition.toWorldSpaceHitbox(entity);
        return create(hitbox, aabb);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HitInfo hitInfo)) return false;
        return Objects.equals(limb, hitInfo.limb);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(limb);
    }
}
