package tnt.tarkovcraft.medsystem.client.util;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public final class UnconsciousModelHelper {

    public static <T extends LivingEntity> void applyPlayerUnconsciousTransforms(PlayerModel<T> model) {
        model.head.xRot = 0.0F;
        model.head.yRot = 0.0F;
        copyRotation(model.head, model.hat);
        model.rightArm.xRot = 0.0F;
        model.rightArm.zRot = (float) Math.toRadians(20.0F);
        copyRotation(model.rightArm, model.rightSleeve);
        model.leftArm.xRot = 0.0F;
        model.leftArm.zRot = (float) Math.toRadians(-40.0F);
        copyRotation(model.leftArm, model.leftSleeve);
        model.rightLeg.xRot = 0.0F;
        model.rightLeg.zRot = (float) Math.toRadians(10.0F);
        copyRotation(model.rightLeg, model.rightPants);
        model.leftLeg.xRot = 0.0F;
        model.leftLeg.zRot = (float) Math.toRadians(-15.0F);
        copyRotation(model.leftLeg, model.leftPants);
    }

    private static void copyRotation(ModelPart from, ModelPart to) {
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
    }
}
