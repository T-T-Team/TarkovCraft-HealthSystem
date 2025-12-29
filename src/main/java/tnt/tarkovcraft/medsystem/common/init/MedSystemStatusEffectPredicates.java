package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.heal.predicate.AnyEffectPredicate;
import tnt.tarkovcraft.medsystem.api.heal.predicate.IsBleedPredicate;
import tnt.tarkovcraft.medsystem.api.heal.predicate.StatusEffectPredicateType;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;

public final class MedSystemStatusEffectPredicates {

    public static final DeferredRegister<StatusEffectPredicateType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.STATUS_EFFECT_PREDICATE, MedSystemConstants.MOD_ID);

    public static final Holder<StatusEffectPredicateType<?>> ANY = REGISTRY.register("any", key -> new StatusEffectPredicateType<>(key, AnyEffectPredicate.CODEC));
    public static final Holder<StatusEffectPredicateType<?>> IS_BLEED = REGISTRY.register("is_bleed", key -> new StatusEffectPredicateType<>(key, IsBleedPredicate.CODEC));
}
