package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class PainStatusEffect extends IntervalAppliedStatusEffect {

    public static final MapCodec<PainStatusEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> common(instance).and(
            Codec.INT.optionalFieldOf("timeActive", 0).forGetter(t -> t.timeActive)
    ).apply(instance, PainStatusEffect::new));

    private int timeActive;

    public PainStatusEffect(int duration) {
        super(duration);
    }

    private PainStatusEffect(int duration, int timeActive) {
        super(duration);
        this.timeActive = timeActive;
    }

    @Override
    public int getUpdateInterval() {
        return 20;
    }

    @Override
    public void applyEffect(LivingEntity entity, Context context) {
        if (!HealthSystem.isInPain(entity)) {
            this.markForRemoval();
        }
        this.timeActive += 20;
    }

    @Override
    public StatusEffect copy() {
        return new PainStatusEffect(this.getDuration(), this.timeActive);
    }

    @Override
    public StatusEffect onRemoved(Context context) {
        return null;
    }

    @Override
    public boolean hasVisibleDuration() {
        return false;
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.PAIN.value();
    }

    public int getTimeActive() {
        return timeActive;
    }
}
