package tnt.tarkovcraft.medsystem.mixin.client;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow public abstract Entity getEntity();

    @Inject(
            method = "setRotation(FFF)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void medsystem$setRotation(CallbackInfo ci) {
        Entity entity = getEntity();
        if (entity instanceof Player player && BloodSystem.isEntityUnconscious(player)) {
            ci.cancel();
        }
    }
}
