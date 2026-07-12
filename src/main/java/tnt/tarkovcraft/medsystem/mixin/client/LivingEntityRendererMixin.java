package tnt.tarkovcraft.medsystem.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.client.RenderStateExtensions;
import tnt.tarkovcraft.medsystem.client.util.UnconsciousModelHelper;
import tnt.tarkovcraft.medsystem.common.blood_system.UnconsciousAnimationState;

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
    private void medsystem$setupRotations(S state, PoseStack poseStack, float bodyRot, float entityScale, CallbackInfo ci) {
        if (!RenderStateExtensions.shouldApplyUnconsciousAttributes(state)) // TODO default pose, smooth anim for wake up too
            return;
        UnconsciousModelHelper.setupHumanoidRotations(state, poseStack, bodyRot);
        ci.cancel();
    }
}
