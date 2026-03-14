package tnt.tarkovcraft.medsystem.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;

@Mixin(Inventory.class)
public abstract class InventoryMixin implements Container, Nameable {

    @Shadow
    @Final
    public Player player;

    // won't work with keybinds, too much effort is needed to block all possible paths to item switch in 1.21.1
    @Inject(
            method = "swapPaint",
            at = @At("HEAD"),
            cancellable = true
    )
    private void medsystem$swapPaint(double direction, CallbackInfo ci) {
        if (BloodSystemManager.isUnconscious(this.player)) {
            ci.cancel();
        }
    }
}
