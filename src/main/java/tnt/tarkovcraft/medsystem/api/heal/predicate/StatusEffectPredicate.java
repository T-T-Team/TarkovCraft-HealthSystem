package tnt.tarkovcraft.medsystem.api.heal.predicate;

import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;

import java.util.function.Predicate;

public interface StatusEffectPredicate extends Predicate<StatusEffect> {

    StatusEffectPredicateType<?> getType();
}
