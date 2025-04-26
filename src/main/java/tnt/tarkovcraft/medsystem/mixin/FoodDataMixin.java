package tnt.tarkovcraft.medsystem.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {

    // This will stop working if minecraft stops processing player food data on single thread

    @Unique
    private ServerPlayer medsystem$player;

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void medsystem$capturePlayer(ServerPlayer player, CallbackInfo ci) {
        this.medsystem$player = player;
    }

    @ModifyVariable(
            method = "tick",
            at = @At("STORE"),
            ordinal = 0
    )
    private boolean medsystem$canRegenerateHealth(boolean naturalRegeneration) {
        return medsystem$player.getData(MedSystemDataAttachments.HEALTH_CONTAINER).canHeal(null, false);
    }
}
