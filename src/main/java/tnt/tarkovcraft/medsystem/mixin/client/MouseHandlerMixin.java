package tnt.tarkovcraft.medsystem.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Shadow @Final private Minecraft minecraft;

    @Inject(
            method = "turnPlayer",
            at = @At("HEAD"),
            cancellable = true
    )
    private void medsystem$turnPlayer(CallbackInfo ci) {
        Player player = this.minecraft.player;
        if (player != null && BloodSystem.isEntityUnconscious(player)) {
            ci.cancel();
        }
    }
}
