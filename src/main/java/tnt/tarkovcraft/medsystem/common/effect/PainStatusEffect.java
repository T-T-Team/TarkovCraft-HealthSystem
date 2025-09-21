package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import javax.annotation.Nullable;
import java.util.Collection;

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
    public void applyEffect(HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
        if (!HealthSystem.isInPain(entity) && this.isInfinite()) {
            this.setDuration(30);
        }
    }

    @Override
    public StatusEffect copy() {
        return new PainStatusEffect(this.getDuration());
    }

    @Override
    public Collection<PostEffect> onRemoved(HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
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
}
