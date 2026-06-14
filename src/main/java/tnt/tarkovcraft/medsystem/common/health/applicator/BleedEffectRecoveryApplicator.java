package tnt.tarkovcraft.medsystem.common.health.applicator;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.api.heal.EffectRecoveryApplicator;
import tnt.tarkovcraft.medsystem.common.config.BleedConfiguration;
import tnt.tarkovcraft.medsystem.common.effect.BleedStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.FreshWoundStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.LimbSelection;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public record BleedEffectRecoveryApplicator(Set<BleedStatusEffect.BleedType> bleedTypes, LimbSelection limbFilter, boolean createWound) implements EffectRecoveryApplicator {

    public static final MapCodec<BleedEffectRecoveryApplicator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codecs.enumSet(BleedStatusEffect.BleedType.CODEC).fieldOf("bleed_types").forGetter(t -> t.bleedTypes),
            LimbSelection.CODEC.optionalFieldOf("limb_filter", LimbSelection.ALL).forGetter(t -> t.limbFilter),
            Codec.BOOL.optionalFieldOf("create_wound", true).forGetter(t -> t.createWound)
    ).apply(instance, BleedEffectRecoveryApplicator::new));

    public static BleedEffectRecoveryApplicator of(boolean wound, LimbSelection filter, BleedStatusEffect.BleedType first, BleedStatusEffect.BleedType... rest) {
        return new BleedEffectRecoveryApplicator(EnumSet.of(first, rest), filter, wound);
    }

    public static BleedEffectRecoveryApplicator of(LimbSelection filter, BleedStatusEffect.BleedType first, BleedStatusEffect.BleedType... rest) {
        return new BleedEffectRecoveryApplicator(EnumSet.of(first, rest), filter, true);
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
        if (this.createWound && effect instanceof BleedStatusEffect bleed) {
            BleedStatusEffect.BleedType bleedType = bleed.getBleedType();
            BleedConfiguration.BleedStageConfig config = bleed.getStageConfiguration();
            BleedStatusEffect.BleedType resultBleedType = switch (bleedType) {
                case CRITICAL -> BleedStatusEffect.BleedType.MODERATE;
                case HEAVY -> BleedStatusEffect.BleedType.LIGHT;
                default -> null;
            };
            if (resultBleedType != null && config.woundDuration > 0) {
                StatusEffectMap effects = limb.getStatusEffects();
                FreshWoundStatusEffect freshWoundStatusEffect = new FreshWoundStatusEffect(config.woundDuration, resultBleedType);
                effects.getEffectSubmitter().submit(Duration.seconds(5), freshWoundStatusEffect);
            }
        }
    }

    @Override
    public void addLabels(Consumer<Component> recoveryLabelAdder, Consumer<Component> noteAdder) {
        this.bleedTypes.forEach(type -> {
            Component bleedTypeLabel = type.getLabel();
            MutableComponent label = SimpleEffectRecoveryApplicator.createDisplayText(bleedTypeLabel);
            this.limbFilter.appendApplicableOnLabel(label);
            recoveryLabelAdder.accept(label);
        });
    }

    @Override
    public MapCodec<? extends EffectRecoveryApplicator> codec() {
        return CODEC;
    }

    private boolean isHealableBleed(BleedStatusEffect bleed) {
        BleedStatusEffect.BleedType bleedType = bleed.getBleedType();
        return this.bleedTypes.contains(bleedType);
    }
}
