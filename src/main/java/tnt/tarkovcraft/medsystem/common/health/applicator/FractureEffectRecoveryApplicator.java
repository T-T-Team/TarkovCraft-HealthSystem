package tnt.tarkovcraft.medsystem.common.health.applicator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.core.util.helper.TextHelper;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.heal.EffectRecoveryApplicator;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.LimbSelection;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTags;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public record FractureEffectRecoveryApplicator(int duration, LimbSelection limbFilter) implements EffectRecoveryApplicator {

    public static final MapCodec<FractureEffectRecoveryApplicator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("duration").forGetter(FractureEffectRecoveryApplicator::duration),
            LimbSelection.CODEC.optionalFieldOf("limb_filter", LimbSelection.ALL).forGetter(FractureEffectRecoveryApplicator::limbFilter)
    ).apply(instance, FractureEffectRecoveryApplicator::new));

    public static FractureEffectRecoveryApplicator of(TickValue duration, LimbSelection limbFilter) {
        return new FractureEffectRecoveryApplicator(duration.tickValue(), limbFilter);
    }

    public static FractureEffectRecoveryApplicator splint() {
        return new FractureEffectRecoveryApplicator(MedicalSystem.getConfig().statusEffects.fractureRecoveryTime, LimbSelection.ARM_LEG);
    }

    public static FractureEffectRecoveryApplicator pressureDressing() {
        return new FractureEffectRecoveryApplicator(MedicalSystem.getConfig().statusEffects.fractureRecoveryTimeBandage, LimbSelection.ALL);
    }

    @Override
    public Optional<StatusEffect> findRecoverableEffect(HealthContainer container, LivingEntity entity, Limb limb) {
        if (!this.limbFilter.test(limb))
            return Optional.empty();
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
    public void addLabels(Consumer<Component> recoveryLabelAdder, Consumer<Component> noteAdder) {
        Component fractureLabel = MedSystemStatusEffects.FRACTURE.value().getDisplayName();
        Component durationLabel = Duration.format(this.duration);
        MutableComponent label = Component.translatable("label.medsystem.effect_recovery.fracture", fractureLabel, durationLabel);
        this.limbFilter.appendApplicableOnLabel(label);
        recoveryLabelAdder.accept(label);
    }

    @Override
    public MapCodec<? extends EffectRecoveryApplicator> codec() {
        return CODEC;
    }

    private boolean isRecoverableFracture(StatusEffect effect) {
        return effect.getType().is(MedSystemTags.StatusEffects.IS_FRACTURE) && effect.isInfinite();
    }
}
