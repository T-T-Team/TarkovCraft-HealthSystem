package tnt.tarkovcraft.medsystem.common.health.applicator;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.api.heal.EffectRecoveryApplicator;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public record StagedEffectRecoveryApplicator(List<EffectRecoveryApplicator> stages) implements EffectRecoveryApplicator {

    public static final MapCodec<StagedEffectRecoveryApplicator> CODEC = EffectRecoveryApplicator.CODEC.listOf()
            .xmap(StagedEffectRecoveryApplicator::new, StagedEffectRecoveryApplicator::stages).fieldOf("stages");

    public static StagedEffectRecoveryApplicator of(EffectRecoveryApplicator... stages) {
        return new StagedEffectRecoveryApplicator(List.of(stages));
    }

    @Override
    public Optional<StatusEffect> findRecoverableEffect(HealthContainer container, LivingEntity entity, Limb limb) {
        for (EffectRecoveryApplicator stage : this.stages) {
            var result = stage.findRecoverableEffect(container, entity, limb);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }

    @Override
    public void apply(HealthContainer container, LivingEntity entity, StatusEffect effect, Limb limb) {
        for (EffectRecoveryApplicator stage : this.stages) {
            var result = stage.findRecoverableEffect(container, entity, limb);
            if (result.isPresent()) {
                stage.apply(container, entity, result.get(), limb);
                break;
            }
        }
    }

    @Override
    public void addLabels(Consumer<Component> recoveryLabelAdder, Consumer<Component> noteAdder) {
        this.stages.forEach(stage -> stage.addLabels(recoveryLabelAdder, noteAdder));
    }

    @Override
    public MapCodec<? extends EffectRecoveryApplicator> codec() {
        return CODEC;
    }
}
