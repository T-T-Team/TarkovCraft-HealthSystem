package tnt.tarkovcraft.medsystem.api.heal.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.effect.BleedStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectPredicates;

public record IsBleedPredicate(boolean heavy) implements StatusEffectPredicate {

    public static final MapCodec<IsBleedPredicate> CODEC = Codec.BOOL
            .xmap(IsBleedPredicate::new, IsBleedPredicate::heavy).fieldOf("heavy_bleed");

    @Override
    public boolean test(StatusEffect effect) {
        if (effect instanceof BleedStatusEffect bleedEffect) {
            return this.heavy == BleedStatusEffect.needsTourniquet(bleedEffect);
        }
        return false;
    }

    @Override
    public StatusEffectPredicateType<?> getType() {
        return MedSystemStatusEffectPredicates.IS_BLEED.value();
    }
}
