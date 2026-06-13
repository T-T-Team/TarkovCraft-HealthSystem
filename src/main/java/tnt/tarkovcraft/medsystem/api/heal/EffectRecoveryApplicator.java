package tnt.tarkovcraft.medsystem.api.heal;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Optional;
import java.util.function.Consumer;

public interface EffectRecoveryApplicator {

    Codec<EffectRecoveryApplicator> CODEC = MedSystemRegistries.EFFECT_RECOVERY_APPLICATOR.byNameCodec()
            .dispatch(EffectRecoveryApplicator::type, EffectRecoveryApplicatorType::codec);

    Optional<StatusEffect> findRecoverableEffect(HealthContainer container, LivingEntity entity, Limb limb);

    void apply(HealthContainer container, LivingEntity entity, StatusEffect effect, Limb limb);

    void addLabels(Consumer<Component> lineAdder);

    EffectRecoveryApplicatorType<?> type();
}
