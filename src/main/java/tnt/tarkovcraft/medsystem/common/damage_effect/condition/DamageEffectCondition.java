package tnt.tarkovcraft.medsystem.common.damage_effect.condition;

import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContextType;

public interface DamageEffectCondition {

    boolean matches(DamageEffectContext context);

    DamageEffectConditionType<?> getType();

    default void validate(DamageEffectContextType contextType) {
    }
}
