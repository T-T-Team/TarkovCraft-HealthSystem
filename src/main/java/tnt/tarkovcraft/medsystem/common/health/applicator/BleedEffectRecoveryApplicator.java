package tnt.tarkovcraft.medsystem.common.health.applicator;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.api.heal.EffectRecoveryApplicator;
import tnt.tarkovcraft.medsystem.api.heal.EffectRecoveryApplicatorType;
import tnt.tarkovcraft.medsystem.common.effect.BleedStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemEffectRecoveryApplicators;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public record BleedEffectRecoveryApplicator(Set<BleedStatusEffect.BleedType> bleedTypes) implements EffectRecoveryApplicator {

    public static final MapCodec<BleedEffectRecoveryApplicator> CODEC = Codecs.enumSet(BleedStatusEffect.BleedType.CODEC)
            .xmap(BleedEffectRecoveryApplicator::new, BleedEffectRecoveryApplicator::bleedTypes).fieldOf("bleed_types");

    public static BleedEffectRecoveryApplicator of(BleedStatusEffect.BleedType first, BleedStatusEffect.BleedType... rest) {
        return new BleedEffectRecoveryApplicator(EnumSet.of(first, rest));
    }

    @Override
    public Optional<StatusEffect> findRecoverableEffect(HealthContainer container, LivingEntity entity, Limb limb) {
        StatusEffectMap statusEffects = limb.getStatusEffects();
        return statusEffects.getEffect(MedSystemStatusEffects.BLEED)
                .filter(effect -> this.isHealableBleed((BleedStatusEffect) effect));
    }

    @Override
    public void apply(HealthContainer container, LivingEntity entity, StatusEffect effect, Limb limb) {
        effect.markForRemoval();
        // TODO fresh wound schedule
    }

    @Override
    public Component getDisplayText() {
        List<String> localizedLabels = this.bleedTypes.stream().map(BleedStatusEffect.BleedType::getLabel)
                .map(Component::getString)
                .toList();
        return Component.translatable("label.medsystem.effect_recovery.simple", String.join(",", localizedLabels));
    }

    @Override
    public EffectRecoveryApplicatorType<?> type() {
        return MedSystemEffectRecoveryApplicators.BLEED.value();
    }

    private boolean isHealableBleed(BleedStatusEffect bleed) {
        BleedStatusEffect.BleedType bleedType = bleed.getBleedType();
        return this.bleedTypes.contains(bleedType);
    }
}
