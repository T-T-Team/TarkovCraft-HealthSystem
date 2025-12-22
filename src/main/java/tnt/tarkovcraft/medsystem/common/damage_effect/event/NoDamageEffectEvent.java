package tnt.tarkovcraft.medsystem.common.damage_effect.event;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectEvents;

public class NoDamageEffectEvent implements DamageEffectEvent {

    public static final NoDamageEffectEvent INSTANCE = new NoDamageEffectEvent();
    public static final MapCodec<NoDamageEffectEvent> CODEC = MapCodec.unit(INSTANCE);

    private NoDamageEffectEvent() {
    }

    @Override
    public void apply(DamageEffectContext context) {
    }

    @Override
    public DamageEffectEventType<?> getType() {
        return MedSystemDamageEffectEvents.NONE.value();
    }
}
