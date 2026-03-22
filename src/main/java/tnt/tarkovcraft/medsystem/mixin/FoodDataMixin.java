package tnt.tarkovcraft.medsystem.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;

@Mixin(FoodData.class)
public abstract class FoodDataMixin {

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
        return naturalRegeneration
                && HealthSystem.hasCustomHealth(medsystem$player)
                && HealthContainer.getAttached(medsystem$player).canHeal()
                && BloodSystemManager.isUnconscious(medsystem$player);
    }
}
