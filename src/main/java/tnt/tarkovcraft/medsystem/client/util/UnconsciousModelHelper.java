package tnt.tarkovcraft.medsystem.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import tnt.tarkovcraft.medsystem.client.RenderStateExtensions;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousAnimationState;

public final class UnconsciousModelHelper {

    public static void setupHumanoidRotations(LivingEntityRenderState state, PoseStack poseStack, float bodyRot) {
        UnconsciousAnimationState animationState = state.getRenderDataOrDefault(RenderStateExtensions.UNCONSCIOUS_ANIMATION, UnconsciousAnimationState.DEFAULT_STATE);
        float collapseAnimAmount = animationState.collapseProgress();
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F - bodyRot));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F * collapseAnimAmount));
        poseStack.translate(0.0, -0.9 * collapseAnimAmount, -0.1 * collapseAnimAmount);
    }

    public static void applyPlayerUnconsciousTransforms(PlayerModel model, UnconsciousAnimationState animationState) {
        float progress = animationState.collapseProgress();
        model.head.xRot = 0.0F;
        model.head.yRot = 0.0F;

        float rightArm = animationState.getMetadataValue("right_arm");
        model.rightArm.xRot = 0.0F;
        model.rightArm.zRot = (float) Math.toRadians(5.0F + 65.0F * rightArm) * progress;

        float leftArm = animationState.getMetadataValue("left_arm");
        model.leftArm.xRot = 0.0F;
        model.leftArm.zRot = (float) Math.toRadians(-5.0F - 65.0F * leftArm) * progress;

        float rightLeg = animationState.getMetadataValue("right_leg");
        model.rightLeg.xRot = 0.0F;
        model.rightLeg.zRot = (float) Math.toRadians(35.0F * rightLeg) * progress;

        float leftLeg = animationState.getMetadataValue("left_leg");
        model.leftLeg.xRot = 0.0F;
        model.leftLeg.zRot = (float) Math.toRadians(-35.0F * leftLeg) * progress;
    }
}
