package tnt.tarkovcraft.medsystem.common.damage_effect.event;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContextType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectEvents;

import java.util.List;

public record ListDamageEffectEvent(List<DamageEffectEvent> items) implements DamageEffectEvent {

    public static final MapCodec<ListDamageEffectEvent> CODEC = DamageEffectEventType.CODEC.listOf(2, Integer.MAX_VALUE)
            .fieldOf("items").xmap(ListDamageEffectEvent::new, ListDamageEffectEvent::items);

    @Override
    public void apply(DamageEffectContext context) {
        this.items.forEach(item -> item.apply(context));
    }

    @Override
    public void validate(DamageEffectContextType type) {
        this.items.forEach(item -> item.validate(type));
    }

    @Override
    public DamageEffectEventType<?> getType() {
        return MedSystemDamageEffectEvents.LIST.value();
    }
}
