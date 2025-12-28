package tnt.tarkovcraft.medsystem.mixin.client;

import net.minecraft.client.renderer.debug.EntityHitboxDebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.LimbType;

@Mixin(EntityHitboxDebugRenderer.class)
public abstract class EntityHitboxDebugRendererMixin {

    @Inject(
            method = "showHitboxes",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/gizmos/Gizmos;cuboid(Lnet/minecraft/world/phys/AABB;Lnet/minecraft/gizmos/GizmoStyle;)Lnet/minecraft/gizmos/GizmoProperties;", ordinal = 2)
    )
    private void medsystem$showHitboxes(Entity entity, float renderTickDelta, boolean applyColor, CallbackInfo ci) {
        if (!HealthSystem.hasCustomHealth(entity))
            return;
        LivingEntity livingEntity = (LivingEntity) entity;
        HealthContainer container = HealthSystem.getHealthData(livingEntity);
        container.iterateHitboxes(livingEntity, (hitbox, limb) -> {
            LimbType limbType = limb.getType();
            Vec3 positionVec = entity.position();
            Vec3 interpolatedPosition = entity.getPosition(renderTickDelta).subtract(positionVec);
            AABB aabb = hitbox.toWorldSpaceHitbox(livingEntity).move(interpolatedPosition);
            Gizmos.cuboid(aabb, GizmoStyle.stroke(limbType.getHitboxColor() | 0xFF << 24));
        });
    }
}
