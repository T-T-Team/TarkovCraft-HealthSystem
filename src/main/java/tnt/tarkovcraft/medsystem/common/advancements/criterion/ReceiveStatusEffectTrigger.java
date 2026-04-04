package tnt.tarkovcraft.medsystem.common.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.server.level.ServerPlayer;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemCriterionTriggers;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.Optional;

public class ReceiveStatusEffectTrigger extends SimpleCriterionTrigger<ReceiveStatusEffectTrigger.TriggerInstance> {

    public static void triggerCriterion(ServerPlayer player, StatusEffect effect) {
        ReceiveStatusEffectTrigger trigger = (ReceiveStatusEffectTrigger) MedSystemCriterionTriggers.RECEIVE_STATUS_EFFECT.value();
        trigger.trigger(player, effect);
    }

    public void trigger(ServerPlayer player, StatusEffect effect) {
        this.trigger(player, trigger -> trigger.matches(effect));
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player,
                                  Optional<HolderSet<StatusEffectType<?>>> effects) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                RegistryCodecs.homogeneousList(MedSystemRegistries.Keys.STATUS_EFFECT).optionalFieldOf("effects").forGetter(TriggerInstance::effects)
        ).apply(i, TriggerInstance::new));

        public boolean matches(StatusEffect effect) {
            return this.effects.isEmpty() || effect.getType().is(this.effects.get());
        }
    }
}
