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

    @Inject(
            method = "setSelectedSlot",
            at = @At("HEAD"),
            cancellable = true
    )
    private void medsystem$setSelectedSlot(int slot, CallbackInfo ci) {
        if (BloodSystemManager.isUnconscious(this.player)) {
            ci.cancel();
        }
    }
}
