package tnt.tarkovcraft.medsystem.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.client.shader.ShaderProcessor;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V", shift = At.Shift.AFTER)
    )
    private void medsystem$render(DeltaTracker tracker, boolean renderLevel, CallbackInfo ci) {
        ShaderProcessor.INSTANCE.render(tracker.getGameTimeDeltaTicks());
    }

    @Inject(
            method = "resize",
            at = @At("HEAD")
    )
    private void medsystem$resize(int width, int height, CallbackInfo ci) {
        ShaderProcessor.INSTANCE.resize(width, height);
    }
}
