package tnt.tarkovcraft.medsystem.common.health.applicator;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.api.heal.EffectRecoveryApplicator;
import tnt.tarkovcraft.medsystem.api.heal.EffectRecoveryApplicatorType;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemEffectRecoveryApplicators;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Optional;

public record SimpleEffectRecoveryApplicator(Holder<StatusEffectType<?>> effect) implements EffectRecoveryApplicator {

    public static final MapCodec<SimpleEffectRecoveryApplicator> CODEC = MedSystemRegistries.STATUS_EFFECT.holderByNameCodec()
            .xmap(SimpleEffectRecoveryApplicator::new, SimpleEffectRecoveryApplicator::effect).fieldOf("effect");

    @Override
    public Optional<StatusEffect> findRecoverableEffect(HealthContainer container, LivingEntity entity, Limb limb) {
        StatusEffectMap statusEffects = limb.getStatusEffects();
        return statusEffects.getEffect(this.effect);
    }

    @Override
    public void apply(HealthContainer container, LivingEntity entity, StatusEffect effect, Limb limb) {
        effect.markForRemoval();
    }

    @Override
    public Component getDisplayText() {
        StatusEffectType<?> type = this.effect.value();
        return Component.translatable("label.medsystem.effect_recovery.simple", type.getDisplayName());
    }

    @Override
    public EffectRecoveryApplicatorType<?> type() {
        return MedSystemEffectRecoveryApplicators.SIMPLE.value();
    }
}
