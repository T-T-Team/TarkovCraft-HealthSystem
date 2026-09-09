package tnt.tarkovcraft.medsystem.integration.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import tnt.tarkovcraft.core.api.SleepFunction;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectType;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;

import java.util.Map;
import java.util.stream.Stream;

public record AdvanceStatusEffectsSleepFunction(Map<EffectType, Float> advanceSpeed) implements SleepFunction {

    public static final MapCodec<AdvanceStatusEffectsSleepFunction> CODEC = Codec.unboundedMap(EffectType.CODEC, ExtraCodecs.NON_NEGATIVE_FLOAT)
            .xmap(AdvanceStatusEffectsSleepFunction::new, AdvanceStatusEffectsSleepFunction::advanceSpeed).fieldOf("advance_speed");

    @Override
    public void apply(ServerPlayer player, long actualSleepDuration) {
        if (!HealthSystem.hasCustomHealth(player))
            return;
        HealthContainer healthContainer = HealthContainer.getAttached(player);
        Stream<StatusEffect> effects = healthContainer.getLimbContainer().getStatusEffects();
        effects.forEach(statusEffect -> {
            if (statusEffect.isInfinite())
                return;
            StatusEffectType<?> type = statusEffect.getType();
            EffectType effectType = type.getEffectType();
            float advanceSpeed = this.advanceSpeed.getOrDefault(effectType, 0.0F);
            if (advanceSpeed <= 0.0F)
                return;
            int advanceAmount = (int) (actualSleepDuration * advanceSpeed);
            int newDuration = statusEffect.getDuration() - advanceAmount;
            statusEffect.setDuration(Math.max(1, newDuration));
        });
        HealthSystem.synchronizeEntity(player);
    }

    @Override
    public MapCodec<? extends SleepFunction> codec() {
        return CODEC;
    }
}
