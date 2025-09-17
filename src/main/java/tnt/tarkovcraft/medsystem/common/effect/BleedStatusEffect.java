package tnt.tarkovcraft.medsystem.common.effect;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.common.MedicalSystemContextKeys;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageTypes;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

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
    public void apply(Context context) {
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
        Level level = entity.level();
        long time = level.getGameTime();
        if (time % this.getDamageInterval() == 0L && level instanceof ServerLevel serverLevel) {
            context.get(MedicalSystemContextKeys.BODY_PART).ifPresent(part -> {
                if (BloodSystem.hasBloodDataIntegration(entity)) {
                    float perMinuteBloodLoss = this.getPerMinuteBloodLossAmount(entity);
                    float bloodLoss = (perMinuteBloodLoss * this.getDamageInterval()) / 1200;
                    BloodSystem.causeBloodLoss(entity, bloodLoss);
                } else {
                    RegistryAccess access = serverLevel.registryAccess();
                    DamageSource damageSource = MedSystemDamageTypes.causeBleedDamage(access, this.getCausingEntity(serverLevel));
                    entity.hurtServer(serverLevel, damageSource, this.getDamageAmount());
                }
            });
        }
    }

    @Override
    public StatusEffect onRemoved(Context context) {
        return null;
    }
}
