package tnt.tarkovcraft.medsystem.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.util.HealthHelper;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "onAttributeUpdated",
            at = @At("RETURN")
    )
    private void medsystem$onAttributeUpdated(Holder<Attribute> holder, CallbackInfo ci) {
        if (hasData(MedSystemDataAttachments.HEALTH_CONTAINER) && holder.is(Attributes.MAX_HEALTH)) {
            LivingEntity livingEntity = (LivingEntity) (Object) this;
            HealthContainer container = HealthContainer.getAttached(livingEntity);
            HealthHelper.synchronizeHealth(livingEntity, container);
        }
    }

    @Inject(
            method = "tick",
            at = @At("RETURN")
    )
    private void medsystem$tick(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (HealthSystem.hasCustomHealth(livingEntity)) {
            HealthContainer container = HealthContainer.getAttached(livingEntity);
            container.tick(livingEntity);
        }
        if (BloodSystemManager.isEnabled(livingEntity)) {
            EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(livingEntity);
            bloodSystem.tick(livingEntity);
        }
    }
}
