package tnt.tarkovcraft.medsystem.common.effect;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;

public class UnconsciousStatusEffect extends SimpleStatusEffect {

    public static final MapCodec<UnconsciousStatusEffect> CODEC = MapCodec.unit(UnconsciousStatusEffect::new);

    public UnconsciousStatusEffect() {
        super(-1);
    }

    @Override
    public StatusEffect copy() {
        return new UnconsciousStatusEffect();
    }

    @Override
    public StatusEffectType<?> getType() {
        return MedSystemStatusEffects.UNCONSCIOUS.value();
    }
}
