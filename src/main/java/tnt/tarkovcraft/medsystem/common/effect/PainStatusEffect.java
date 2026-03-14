package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class PainStatusEffect extends IntervalAppliedStatusEffect {

    public static final MapCodec<PainStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).apply(instance, PainStatusEffect::new));

    public PainStatusEffect(int duration) {
        super(duration);
    }

    public static PainStatusEffect infinite() {
        return new PainStatusEffect(INFINITE_DURATION);
    }

    @Override
    public int getUpdateInterval() {
        return 20;
    }

    @Override
    public void applyEffect(StatusEffectContext context) {
        LivingEntity entity = context.entity();
        if (!HealthSystem.isInPain(entity) && this.isInfinite()) {
            this.setDuration(30);
        }
    }

    @Override
    public StatusEffect copy() {
        return new PainStatusEffect(this.getDuration());
    }

    @Override
    public void onRemoved(StatusEffectContext context) {
    }

    @Override
    public boolean hasVisibleDuration() {
        return false;
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.PAIN.value();
    }
}
