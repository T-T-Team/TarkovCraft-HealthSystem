package tnt.tarkovcraft.medsystem.mixin.client;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.HitboxRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.client.RenderStateExtensions;
import tnt.tarkovcraft.medsystem.common.health.BodyPartHitbox;
import tnt.tarkovcraft.medsystem.common.health.LimbDefinition;
import tnt.tarkovcraft.medsystem.common.health.LimbType;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends EntityRenderer<T, S> {

    public LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(
            method = "extractAdditionalHitboxes(Lnet/minecraft/world/entity/LivingEntity;Lcom/google/common/collect/ImmutableList$Builder;F)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void medsystem$extractAdditionalHitboxes(T entity, ImmutableList.Builder<HitboxRenderState> builder, float delta, CallbackInfo ci) {
        MedicalSystem.HEALTH_SYSTEM.getHealthContainer(entity).ifPresent(container -> {
            for (BodyPartHitbox hitbox : container.getHitboxes()) {
                LimbDefinition healthTpl = container.getLimbConfiguration(hitbox.getOwner());
                if (healthTpl == null)
                    continue;
                LimbType group = healthTpl.getBodyPartGroup();
                int color = group.getHitboxColor();
                float red = ARGB.redFloat(color);
                float green = ARGB.greenFloat(color);
                float blue = ARGB.blueFloat(color);
                AABB aabb = hitbox.transform(entity).aabb();
                builder.add(new HitboxRenderState(
                        aabb.minX, aabb.minY, aabb.minZ,
                        aabb.maxX, aabb.maxY, aabb.maxZ,
                        red, green, blue
                ));
            }
            ci.cancel();
        });
    }

    @Inject(
            method = "setupRotations",
            at = @At("HEAD"),
            cancellable = true
    )
    private void medsystem$setupRotations(S renderState, PoseStack poseStack, float bodyRot, float scale, CallbackInfo ci) {
        if (!RenderStateExtensions.shouldApplyUnconsciousAttributes(renderState))
            return;

        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F - bodyRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(270.0F));
        poseStack.translate(0.0, -0.9, -0.1);
        ci.cancel();
    }
}
