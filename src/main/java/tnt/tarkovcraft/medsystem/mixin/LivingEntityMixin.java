package tnt.tarkovcraft.medsystem.mixin;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tnt.tarkovcraft.medsystem.common.DamageHandler;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.util.HealthHelper;

import java.util.Stack;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    @Shadow
    @Nullable
    protected Stack<DamageContainer> damageContainers;

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

    // FIXME: Temporary workaround, have new event in neoforge?
    @Inject(
            method = "actuallyHurt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;gameEvent(Lnet/minecraft/core/Holder;)V", shift = At.Shift.AFTER)
    )
    private void medsystem$actuallyHurt(ServerLevel level, DamageSource source, float damage, CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        DamageHandler.applyDamage(livingEntity, source, damageContainers);
    }
}
