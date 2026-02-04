package tnt.tarkovcraft.medsystem.client.debug;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.DamageHandler;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationResultDebugInfo;
import tnt.tarkovcraft.medsystem.common.health.calc.HitInfo;
import tnt.tarkovcraft.medsystem.common.health.calc.Ray;

public class HitResultInfoDebugRenderer implements DebugRenderer.SimpleDebugRenderer {

    public static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("debug/hit_result_info");
    public static final HitResultInfoDebugRenderer INSTANCE = new HitResultInfoDebugRenderer();

    private HitResultInfoDebugRenderer() {}

    @Override
    public void emitGizmos(double p_113509_, double p_113510_, double p_113511_, DebugValueAccess p_449770_, Frustum p_451544_, float p_455520_) {
        HitCalculationResultDebugInfo debugInfo = DamageHandler.getHitDebugInfo();
        if (debugInfo == null)
            return;

        for (AABB aabb : debugInfo.entityAABBs()) {
            Gizmos.cuboid(aabb, GizmoStyle.stroke(0xFFFFFFFF));
        }
        for (Ray ray : debugInfo.raycasts()) {
            Vec3 p1 = ray.from();
            Vec3 p2 = ray.to();
            Gizmos.line(p1, p2, 0xFFFF0000, 2.0F);
        }
        for (HitInfo hit : debugInfo.hits()) {
            AABB aabb = hit.aabb();
            Gizmos.cuboid(aabb, GizmoStyle.fill(0x6600FF00));
            Vec3 entry = hit.entryPoint();
            if (entry != null) {
                float scale = 0.02F;
                AABB box = new AABB(entry.x - scale, entry.y - scale, entry.z - scale, entry.x + scale, entry.y + scale, entry.z + scale);
                Gizmos.cuboid(box, GizmoStyle.fill(0xFFFFFF00));
            }
        }
    }
}
