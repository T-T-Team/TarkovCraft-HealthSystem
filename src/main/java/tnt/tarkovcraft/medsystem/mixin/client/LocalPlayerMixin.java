package tnt.tarkovcraft.medsystem.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Inject(
            method = "serverAiStep",
            at = @At("HEAD"),
            cancellable = true
    )
    private void medsystem$applyInput(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer)(Object)this;
        if (BloodSystemManager.isUnconscious(player)) {
            ci.cancel();
        }
    }

    @Inject(
            method = "isShiftKeyDown",
            at = @At("HEAD"),
            cancellable = true
    )
    private void isShiftKeyDown(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = (LocalPlayer)(Object)this;
        if (BloodSystemManager.isUnconscious(player)) {
            cir.setReturnValue(false);
        }
    }
}
