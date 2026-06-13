package tnt.tarkovcraft.medsystem.common.health.applicator;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.medsystem.api.heal.EffectRecoveryApplicator;
import tnt.tarkovcraft.medsystem.api.heal.EffectRecoveryApplicatorType;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemEffectRecoveryApplicators;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTags;

import java.util.Optional;

public record FractureEffectRecoveryApplicator(int duration) implements EffectRecoveryApplicator {

    public static final MapCodec<FractureEffectRecoveryApplicator> CODEC = ExtraCodecs.POSITIVE_INT
            .xmap(FractureEffectRecoveryApplicator::new, FractureEffectRecoveryApplicator::duration).fieldOf("duration");

    public static FractureEffectRecoveryApplicator of(TickValue duration) {
        return new FractureEffectRecoveryApplicator(duration.tickValue());
    }

    @Override
    public Optional<StatusEffect> findRecoverableEffect(HealthContainer container, LivingEntity entity, Limb limb) {
        StatusEffectMap statusEffects = limb.getStatusEffects();
        return statusEffects.getEffectsStream()
                .filter(this::isRecoverableFracture)
                .findFirst();
    }

    @Override
    public void apply(HealthContainer container, LivingEntity entity, StatusEffect effect, Limb limb) {
        effect.setDuration(Math.max(this.duration, 1));
    }

    @Override
    public Component getDisplayText() {
        Component fractureLabel = MedSystemStatusEffects.FRACTURE.value().getDisplayName();
        Component durationLabel = Duration.format(this.duration);
        return Component.translatable("label.medsystem.effect_recovery.fracture", fractureLabel, durationLabel);
    }

    @Override
    public EffectRecoveryApplicatorType<?> type() {
        return MedSystemEffectRecoveryApplicators.FRACTURE.value();
    }

    private boolean isRecoverableFracture(StatusEffect effect) {
        return effect.getType().is(MedSystemTags.StatusEffects.IS_FRACTURE)
                && (effect.isInfinite() || effect.getDuration() > this.duration);
    }
}
