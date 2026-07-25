package tnt.tarkovcraft.medsystem.client.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousAnimationState;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousState;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;

public final class UnconsciousModelHelper {

    // stored partial tick value from LivingEntityRenderer#render method to be used for animations where this value is not accessible
    public static float partialTick;

    public static boolean shouldAnimateUnconscious(LivingEntity entity) {
        if (entity.isPassenger() || !BloodSystemManager.isEnabled(entity))
            return false;
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(entity);
        UnconsciousState state = bloodSystem.getUnconsciousState();
        return bloodSystem.isUnconscious() && state.shouldAnimate();
    }

    public static void setupHumanoidRotations(LivingEntity entity, PoseStack poseStack, float bodyRot, float partialTick) {
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(entity);
        UnconsciousAnimationState animationState = getUnconsciousAnimationState(bloodSystem, partialTick);
        float collapseAnimAmount = animationState.collapseProgress();
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F - bodyRot));
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F * collapseAnimAmount));
        poseStack.translate(0.0, -0.9 * collapseAnimAmount, -0.1 * collapseAnimAmount);
    }

    public static UnconsciousAnimationState getUnconsciousAnimationState(EntityBloodSystem bloodSystem, float delta) {
        UnconsciousAnimationState animationState = bloodSystem.getUnconsciousAnimationState(delta);
        return animationState != null ? animationState : UnconsciousAnimationState.DEFAULT_STATE;
    }

    public static <T extends LivingEntity> void applyPlayerUnconsciousTransforms(PlayerModel<T> model, UnconsciousAnimationState animationState) {
        float progress = animationState.collapseProgress();
        model.head.xRot = 0.0F;
        model.head.yRot = 0.0F;
        copyRotation(model.head, model.hat);

        float rightArm = animationState.getMetadataValue("right_arm");
        model.rightArm.xRot = 0.0F;
        model.rightArm.zRot = (float) Math.toRadians(5.0F + 65.0F * rightArm) * progress;
        copyRotation(model.rightArm, model.rightSleeve);

        float leftArm = animationState.getMetadataValue("left_arm");
        model.leftArm.xRot = 0.0F;
        model.leftArm.zRot = (float) Math.toRadians(-5.0F - 65.0F * leftArm) * progress;
        copyRotation(model.leftArm, model.leftSleeve);

        float rightLeg = animationState.getMetadataValue("right_leg");
        model.rightLeg.xRot = 0.0F;
        model.rightLeg.zRot = (float) Math.toRadians(35.0F * rightLeg) * progress;
        copyRotation(model.rightLeg, model.rightPants);

        float leftLeg = animationState.getMetadataValue("left_leg");
        model.leftLeg.xRot = 0.0F;
        model.leftLeg.zRot = (float) Math.toRadians(-35.0F * leftLeg) * progress;
        copyRotation(model.leftLeg, model.leftPants);
    }

    private static void copyRotation(ModelPart from, ModelPart to) {
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
    }
}
