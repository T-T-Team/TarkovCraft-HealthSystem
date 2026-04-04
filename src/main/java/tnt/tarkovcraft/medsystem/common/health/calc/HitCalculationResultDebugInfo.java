package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import tnt.tarkovcraft.medsystem.util.HitboxHelper;

import java.util.List;

public record HitCalculationResultDebugInfo(AABB entityAABB, List<AABB> entityAABBs, List<HitInfo> hits, List<Ray> raycasts) {

    public static HitCalculationResultDebugInfo collectDebugData(HitCalculationContext context, HitCalculationResult result) {
        LivingEntity entity = context.entity();
        List<AABB> aabbs = HitboxHelper.getEntityHitboxes(context)
                .map(hitbox -> hitbox.worldspaceAABB(entity))
                .toList();
        return new HitCalculationResultDebugInfo(entity.getBoundingBox(), aabbs, result.getHits(), result.getRaycasts());
    }
}
