package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import tnt.tarkovcraft.medsystem.common.health.EntityHitboxContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

public record LimbHitbox(EntityHitboxContainer.LimbHitboxDefinition definition, Limb limb) {

    public AABB worldspaceAABB(LivingEntity ctx) {
        return definition.toWorldSpaceHitbox(ctx);
    }
}
