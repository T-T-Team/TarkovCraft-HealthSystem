package tnt.tarkovcraft.medsystem.mixin.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.client.RenderStateExtensions;

@Mixin(PlayerModel.class)
public abstract class PlayerModelMixin extends HumanoidModel<AvatarRenderState> {

    public PlayerModelMixin(ModelPart root) {
        super(root);
    }

    @Inject(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V"),
            cancellable = true
    )
    private void medsystem$setupAnim(AvatarRenderState renderState, CallbackInfo ci) {
        if (!RenderStateExtensions.shouldApplyUnconsciousAttributes(renderState))
            return;
        PlayerModel model = (PlayerModel) (Object) this;
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
        ci.cancel();
    }
}
