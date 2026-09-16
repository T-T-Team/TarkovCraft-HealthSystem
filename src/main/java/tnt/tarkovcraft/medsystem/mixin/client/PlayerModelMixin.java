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
import tnt.tarkovcraft.medsystem.client.util.UnconsciousModelHelper;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousAnimationState;

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
    private void medsystem$setupAnim(AvatarRenderState state, CallbackInfo ci) {
        if (!RenderStateExtensions.shouldApplyUnconsciousAttributes(state) || !RenderStateExtensions.hasSpecialPoseRenderer(state))
            return;
        UnconsciousAnimationState animationState = state.getRenderDataOrDefault(RenderStateExtensions.UNCONSCIOUS_ANIMATION, UnconsciousAnimationState.DEFAULT_STATE);
        PlayerModel model = (PlayerModel) (Object) this;
        UnconsciousModelHelper.applyPlayerUnconsciousTransforms(model, animationState);
        ci.cancel();
    }
}
