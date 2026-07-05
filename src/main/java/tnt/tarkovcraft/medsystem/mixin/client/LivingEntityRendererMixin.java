package tnt.tarkovcraft.medsystem.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.client.RenderStateExtensions;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> {

    public LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(
            method = "setupRotations",
            at = @At("HEAD"),
            cancellable = true
    )
    private void medsystem$setupRotations(S renderState, PoseStack poseStack, float bodyRot, float scale, CallbackInfo ci) {
        if (!RenderStateExtensions.shouldApplyUnconsciousAttributes(renderState)) // TODO default pose, smooth anim for wake up too
            return;
        float collapseAnimAmount = renderState.getRenderDataOrDefault(RenderStateExtensions.COLLAPSE_ANIMATION_AMOUNT, 1.0F);

        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F - bodyRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F * collapseAnimAmount));
        poseStack.mulPose(Axis.YP.rotationDegrees(270.0F * collapseAnimAmount));
        poseStack.translate(0.0, -0.9 * collapseAnimAmount, -0.15 * collapseAnimAmount);
        ci.cancel();
    }
}
