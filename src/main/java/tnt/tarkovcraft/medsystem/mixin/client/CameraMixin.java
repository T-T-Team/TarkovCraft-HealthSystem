package tnt.tarkovcraft.medsystem.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow private Entity entity;

    @Inject(
            method = "setRotation(FFF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void medsystem$setRotation(CallbackInfo ci) {
        if (this.entity instanceof LivingEntity livingEntity && BloodSystemManager.isUnconscious(livingEntity)) {
            ci.cancel();
        }
    }
}
