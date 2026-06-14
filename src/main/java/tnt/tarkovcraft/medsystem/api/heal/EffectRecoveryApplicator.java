package tnt.tarkovcraft.medsystem.api.heal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface EffectRecoveryApplicator {

    Codec<EffectRecoveryApplicator> CODEC = MedSystemRegistries.EFFECT_RECOVERY_APPLICATOR.byNameCodec()
            .dispatch(EffectRecoveryApplicator::codec, Function.identity());

    Optional<StatusEffect> findRecoverableEffect(HealthContainer container, LivingEntity entity, Limb limb);

    void apply(HealthContainer container, LivingEntity entity, StatusEffect effect, Limb limb);

    void addLabels(Consumer<Component> lineAdder);

    MapCodec<? extends EffectRecoveryApplicator> codec();
}
