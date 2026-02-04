package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.util.HitboxHelper;

import java.util.List;

public record HitCalculationResultDebugInfo(List<AABB> entityAABBs, List<HitInfo> hits, List<Ray> raycasts) {

    public static HitCalculationResultDebugInfo collectDebugData(HitCalculationContext context, HitCalculationResult result) {
        LivingEntity entity = context.entity();
        List<AABB> aabbs = HitboxHelper.getEntityHitboxes(context)
                .map(hitbox -> hitbox.worldspaceAABB(entity))
                .toList();
        return new HitCalculationResultDebugInfo(aabbs, result.getHits(), result.getRaycasts());
    }

    public void submitRenderData() {

    }
}
