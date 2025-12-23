package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class PainStatusEffect extends IntervalAppliedStatusEffect {

    public static final MapCodec<PainStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).apply(instance, PainStatusEffect::new));

    public PainStatusEffect(int duration) {
        super(duration);
    }

    @Override
    public int getUpdateInterval() {
        return 20;
    }

    @Override
    public void applyEffect(HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        if (!HealthSystem.isInPain(entity) && this.isInfinite()) {
            this.setDuration(30);
        }
    }

    @Override
    public StatusEffect copy() {
        return new PainStatusEffect(this.getDuration());
    }

    @Override
    public void onRemoved(StatusEffectSubmitter submitter, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
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
