package tnt.tarkovcraft.medsystem.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.common.DamageHandler;

@Mixin(Player.class)
public abstract class PlayerMixin extends Avatar implements ContainerUser {

    public PlayerMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    // FIXME: Temporary workaround, have new event in neoforge?
    @Inject(
            method = "actuallyHurt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;gameEvent(Lnet/minecraft/core/Holder;)V", shift = At.Shift.AFTER)
    )
    private void medsystem$actuallyHurt(ServerLevel level, DamageSource source, float damage, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        DamageHandler.applyDamage(player, source, damageContainers);
    }
}
