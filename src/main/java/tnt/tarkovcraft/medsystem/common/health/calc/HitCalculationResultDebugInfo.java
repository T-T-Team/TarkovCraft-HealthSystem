package tnt.tarkovcraft.medsystem.common.health.calc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
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

    @OnlyIn(Dist.CLIENT)
    public void submitRenderData(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double camX, double camY, double camZ) {
        poseStack.pushPose();
        Matrix4f pose = poseStack.last().pose();
        poseStack.translate(-camX, -camY, -camZ);
        for (Ray ray : this.raycasts) {
            Vec3 p1 = ray.from();
            Vec3 p2 = ray.to();
            VertexConsumer line = bufferSource.getBuffer(RenderType.debugLineStrip(2.0F));
            line.addVertex(pose, (float) p1.x, (float) p1.y, (float) p1.z).setColor(1.0F, 0.0F, 0.0F, 1.0F);
            line.addVertex(pose, (float) p2.x, (float) p2.y, (float) p2.z).setColor(1.0F, 0.0F, 0.0F, 1.0F);
        }
        for (HitInfo hit : this.hits) {
            AABB aabb = hit.aabb();
            Vec3 entry = hit.entryPoint();
            if (entry != null) {
                float scale = 0.02F;
                AABB box = new AABB(entry.x - scale, entry.y - scale, entry.z - scale, entry.x + scale, entry.y + scale, entry.z + scale);
                DebugRenderer.renderFilledBox(poseStack, bufferSource, box, 1.0F, 1.0F, 0.0F, 1.0F);
            }
            DebugRenderer.renderFilledBox(poseStack, bufferSource, aabb, 0.0F, 1.0F, 0.0F, 0.3F);
        }
        for (AABB aabb : this.entityAABBs) {
            DebugRenderer.renderFilledBox(poseStack, bufferSource, aabb, 1.0F, 1.0F, 1.0F, 0.2F);
        }

        poseStack.popPose();
    }
}
