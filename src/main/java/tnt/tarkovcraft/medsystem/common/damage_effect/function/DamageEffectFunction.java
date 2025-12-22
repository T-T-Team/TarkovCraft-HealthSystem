package tnt.tarkovcraft.medsystem.common.damage_effect.function;

import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContextType;

public interface DamageEffectFunction {

    int apply(int duration, DamageEffectContext context);

    DamageEffectFunctionType<?> getType();

    default void validate(DamageEffectContextType type) {
    }
}
