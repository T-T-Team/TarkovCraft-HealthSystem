package tnt.tarkovcraft.medsystem.common.effect;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageTypes;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public abstract class BleedStatusEffect extends EntityCausedStatusEffect {

    public BleedStatusEffect(int duration, Optional<UUID> owner) {
        super(duration, owner);
    }

    public BleedStatusEffect(int duration) {
        super(duration);
    }

    public abstract float getPerMinuteBloodLossAmount(LivingEntity entity);

    public abstract long getDamageInterval();

    public abstract float getDamageAmount();

    @Override
    public void apply(HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        Level level = entity.level();
        long time = level.getGameTime();
        if (limb != null && time % this.getDamageInterval() == 0L && level instanceof ServerLevel serverLevel) {
            if (BloodSystem.hasBloodDataIntegration(entity)) {
                float perMinuteBloodLoss = this.getPerMinuteBloodLossAmount(entity);
                float bloodLoss = (perMinuteBloodLoss * this.getDamageInterval()) / 1200;
                BloodSystem.causeBloodLoss(entity, bloodLoss);
            } else {
                RegistryAccess access = serverLevel.registryAccess();
                DamageSource damageSource = MedSystemDamageTypes.causeBleedDamage(access, this.getCausingEntity(serverLevel));
                entity.hurt(damageSource, this.getDamageAmount());
            }
        }
    }

    @Override
    public void onRemoved(StatusEffectSubmitter submitter, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
    }
}
