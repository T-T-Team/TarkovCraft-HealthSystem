package tnt.tarkovcraft.medsystem.common.health.applicator;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.api.heal.EffectRecoveryApplicator;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Optional;
import java.util.function.Consumer;

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
    public void addLabels(Consumer<Component> recoveryLabelAdder, Consumer<Component> noteAdder) {
        StatusEffectType<?> type = this.effect.value();
        recoveryLabelAdder.accept(createDisplayText(type.getDisplayName()));
    }

    @Override
    public MapCodec<? extends EffectRecoveryApplicator> codec() {
        return CODEC;
    }

    public static MutableComponent createDisplayText(Component effect) {
        return Component.translatable("label.medsystem.effect_recovery.simple", effect);
    }
}
