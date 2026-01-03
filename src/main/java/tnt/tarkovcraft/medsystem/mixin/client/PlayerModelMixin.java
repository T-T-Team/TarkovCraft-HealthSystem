package tnt.tarkovcraft.medsystem.mixin.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.client.UnconsciousPoseHelper;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> {

    public PlayerModelMixin(ModelPart root) {
        super(root);
    }

    @SuppressWarnings("unchecked")
    @Inject(
            method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V"),
            cancellable = true
    )
    private void medsystem$setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!UnconsciousPoseHelper.shouldApplyUnconsciousAttributes(entity))
            return;
        PlayerModel<T> model = (PlayerModel<T>) (Object) this;
        model.head.xRot = 0.0F;
        model.head.yRot = 0.0F;
        model.rightArm.xRot = 0.0F;
        model.rightArm.zRot = (float) Math.toRadians(20.0F);
        model.rightSleeve.xRot = model.rightArm.xRot;
        model.rightSleeve.zRot = model.rightArm.zRot;
        model.leftArm.xRot = 0.0F;
        model.leftArm.zRot = (float) Math.toRadians(-40.0F);
        model.leftSleeve.xRot = model.leftArm.xRot;
        model.leftSleeve.zRot = model.leftArm.zRot;
        model.rightLeg.xRot = 0.0F;
        model.rightLeg.zRot = (float) Math.toRadians(10.0F);
        model.rightPants.xRot = model.rightLeg.xRot;
        model.rightPants.zRot = model.rightLeg.zRot;
        model.leftLeg.xRot = 0.0F;
        model.leftLeg.zRot = (float) Math.toRadians(-15.0F);
        model.leftPants.xRot = model.leftLeg.xRot;
        model.leftPants.zRot = model.leftLeg.zRot;
        ci.cancel();
    }
}
