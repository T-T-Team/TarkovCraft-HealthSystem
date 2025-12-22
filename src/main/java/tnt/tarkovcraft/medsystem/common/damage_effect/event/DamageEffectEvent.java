package tnt.tarkovcraft.medsystem.common.damage_effect.event;

import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContextType;

public interface DamageEffectEvent {

    void apply(DamageEffectContext context);

    DamageEffectEventType<?> getType();

    default void validate(DamageEffectContextType type) {
    }
}
