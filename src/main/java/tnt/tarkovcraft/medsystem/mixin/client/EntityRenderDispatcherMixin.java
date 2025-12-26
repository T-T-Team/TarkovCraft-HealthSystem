package tnt.tarkovcraft.medsystem.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.core.util.helper.ARGB;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.BodyPartHitbox;
import tnt.tarkovcraft.medsystem.common.health.LimbDefinition;
import tnt.tarkovcraft.medsystem.common.health.LimbType;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin implements ResourceManagerReloadListener {

    @Inject(
            method = "renderHitbox",
            at = @At("HEAD")
    )
    private static void medsystem$renderHitbox(PoseStack poseStack, VertexConsumer buffer, Entity entity, float delta, float f0, float f1, float f2, CallbackInfo ci) {
        MedicalSystem.HEALTH_SYSTEM.getHealthContainer(entity.getType()).ifPresent(container -> {
            for (BodyPartHitbox hitbox : container.getHitboxes()) {
                LimbDefinition healthTpl = container.getLimbConfiguration(hitbox.getOwner());
                if (healthTpl == null)
                    continue;
                LimbType group = healthTpl.type();
                int color = group.getHitboxColor();
                float red = ARGB.redFloat(color);
                float green = ARGB.greenFloat(color);
                float blue = ARGB.blueFloat(color);
                AABB aabb = hitbox.transform((LivingEntity) entity).aabb();
                LevelRenderer.renderLineBox(poseStack, buffer, aabb, red, green, blue, 1.0F);
            }
        });
    }
}
