package tnt.tarkovcraft.medsystem.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.client.UnconsciousPoseHelper;
import tnt.tarkovcraft.medsystem.client.util.UnconsciousModelHelper;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity> extends EntityRenderer<T> {

    public LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(
            method = "setupRotations",
            at = @At("HEAD"),
            cancellable = true
    )
    private void medsystem$setupRotations(T entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale, CallbackInfo ci) {
        if (!UnconsciousPoseHelper.shouldApplyUnconsciousAttributes(entity))
            return;
        UnconsciousModelHelper.setupHumanoidRotations(entity, poseStack, yBodyRot, partialTick);
        ci.cancel();
    }

    @Inject(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;prepareMobModel(Lnet/minecraft/world/entity/Entity;FFF)V")
    )
    private void medsystem$render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        UnconsciousModelHelper.partialTick = partialTicks; // capture partial tick value
    }
}
