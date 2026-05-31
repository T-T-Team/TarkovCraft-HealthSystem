package tnt.tarkovcraft.medsystem.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DeathProtection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.LimbContainer;
import tnt.tarkovcraft.medsystem.util.HealthHelper;

@Mixin(DeathProtection.class)
public abstract class DeathProtectionMixin {

    @Inject(
            method = "applyEffects",
            at = @At("HEAD")
    )
    private void medsystem$applyEffects(ItemStack itemStack, LivingEntity entity, CallbackInfo ci) {
        if (!HealthSystem.hasCustomHealth(entity))
            return;
        HealthContainer container = HealthContainer.getAttached(entity);
        LimbContainer limbContainer = container.getLimbContainer();
        limbContainer.forEach(limb -> limb.healUpTo(1.0F));
        HealthHelper.synchronizeHealth(entity, container);
        HealthSystem.synchronizeEntity(entity);

        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(entity);
        if (bloodSystem != null) {
            bloodSystem.removeShock(bloodSystem.getShockAmount());
            bloodSystem.synchronizeImmediately(entity);
        }
    }
}
