package tnt.tarkovcraft.medsystem.common.effect;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.api.BodyPartDamageSource;
import tnt.tarkovcraft.medsystem.common.MedicalSystemContextKeys;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageTypes;

public abstract class BleedStatusEffect extends StatusEffect {

    public BleedStatusEffect(int duration, int delay) {
        super(duration, delay);
    }

    public abstract long getDamageInterval();

    public abstract float getDamageAmount();

    @Override
    public void apply(Context context) {
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
        Level level = entity.level();
        long time = level.getGameTime();
        if (time % this.getDamageInterval() == 0L) {
            context.get(MedicalSystemContextKeys.BODY_PART).ifPresent(part -> {
                Holder<DamageType> type = MedSystemDamageTypes.of(entity.registryAccess(), MedSystemDamageTypes.BLEED);
                BodyPartDamageSource source = new BodyPartDamageSource(type, part.getName());
                source.setAllowDeadBodyPartDamage(false);
                entity.hurt(source, this.getDamageAmount());
            });
        }
    }

    @Override
    public StatusEffect onRemoved(Context context) {
        return null;
    }
}
