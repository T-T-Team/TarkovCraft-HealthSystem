package tnt.tarkovcraft.medsystem.mixin.client;

import com.google.common.collect.ImmutableList;
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
import tnt.tarkovcraft.medsystem.client.MedicalSystemClient;
import tnt.tarkovcraft.medsystem.client.config.MedSystemClientConfig;
import tnt.tarkovcraft.medsystem.common.health.BodyPartGroup;
import tnt.tarkovcraft.medsystem.common.health.BodyPartDefinition;
import tnt.tarkovcraft.medsystem.common.health.BodyPartHitbox;

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
        MedSystemClientConfig config = MedicalSystemClient.getConfig();
        if (!config.enableHitboxDebugRenderer)
            return;
        MedicalSystem.HEALTH_SYSTEM.getHealthContainer(entity).ifPresent(container -> {
            for (BodyPartHitbox hitbox : container.getHitboxes()) {
                BodyPartDefinition healthTpl = container.getHealthTpl(hitbox.getOwner());
                if (healthTpl == null)
                    continue;
                BodyPartGroup group = healthTpl.getBodyPartGroup();
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
}
