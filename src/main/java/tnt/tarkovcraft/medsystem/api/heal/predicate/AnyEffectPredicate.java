package tnt.tarkovcraft.medsystem.api.heal.predicate;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectPredicates;

public final class AnyEffectPredicate implements StatusEffectPredicate {

    public static final AnyEffectPredicate INSTANCE = new AnyEffectPredicate();
    public static final MapCodec<AnyEffectPredicate> CODEC = MapCodec.unit(INSTANCE);

    private AnyEffectPredicate() {}

    @Override
    public boolean test(StatusEffect effect) {
        return true;
    }

    @Override
    public StatusEffectPredicateType<?> getType() {
        return MedSystemStatusEffectPredicates.ANY.value();
    }
}
