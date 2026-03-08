package tnt.tarkovcraft.medsystem.client.util;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;

public final class UnconsciousModelHelper {

    public static <T extends LivingEntity> void applyPlayerUnconsciousTransforms(PlayerModel<T> model) {
        model.head.xRot = 0.0F;
        model.head.yRot = 0.0F;
        model.rightArm.xRot = 0.0F;
        model.rightArm.zRot = (float) Math.toRadians(20.0F);
        model.leftArm.xRot = 0.0F;
        model.leftArm.zRot = (float) Math.toRadians(-40.0F);
        model.rightLeg.xRot = 0.0F;
        model.rightLeg.zRot = (float) Math.toRadians(10.0F);
        model.leftLeg.xRot = 0.0F;
        model.leftLeg.zRot = (float) Math.toRadians(-15.0F);
    }
}
