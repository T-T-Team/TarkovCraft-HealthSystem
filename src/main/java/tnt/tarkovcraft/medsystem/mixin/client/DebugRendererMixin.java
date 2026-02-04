package tnt.tarkovcraft.medsystem.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.common.DamageHandler;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationResultDebugInfo;

@Mixin(DebugRenderer.class)
public abstract class DebugRendererMixin {

    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    private void medsystem$renderHitInfo(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, double camX, double camY, double camZ, CallbackInfo ci) {
        HitCalculationResultDebugInfo debugInfo = DamageHandler.getHitDebugInfo();
        if (debugInfo == null)
            return;
        debugInfo.submitRenderData(poseStack, bufferSource, camX, camY, camZ);
    }
}
