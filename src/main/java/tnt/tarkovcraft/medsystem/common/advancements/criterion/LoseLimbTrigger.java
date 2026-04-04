package tnt.tarkovcraft.medsystem.common.advancements.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemCriterionTriggers;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public final class LoseLimbTrigger extends SimpleCriterionTrigger<LoseLimbTrigger.TriggerInstance> {

    public static void triggerCriterion(ServerPlayer player, Limb limb) {
        LoseLimbTrigger trigger = (LoseLimbTrigger) MedSystemCriterionTriggers.LOSE_LIMB.value();
        trigger.trigger(player, limb);
    }

    public void trigger(ServerPlayer player, Limb limb) {
        this.trigger(player, trigger -> trigger.matches(limb));
    }

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, Set<LimbType> limbs) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codecs.enumSet(LimbType.CODEC).optionalFieldOf("limbs", Collections.emptySet()).forGetter(TriggerInstance::limbs)
        ).apply(instance, TriggerInstance::new));

        public boolean matches(Limb limb) {
            return this.limbs.isEmpty() || this.limbs.contains(limb.getType());
        }
    }
}
