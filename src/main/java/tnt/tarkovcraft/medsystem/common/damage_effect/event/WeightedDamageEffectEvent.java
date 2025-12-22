package tnt.tarkovcraft.medsystem.common.damage_effect.event;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContextType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectEvents;
import tnt.tarkovcraft.medsystem.util.WeightedList;

public record WeightedDamageEffectEvent(WeightedList<DamageEffectEvent> entries) implements DamageEffectEvent {

    public static final MapCodec<WeightedDamageEffectEvent> CODEC = WeightedList.codec(DamageEffectEventType.CODEC)
            .xmap(WeightedDamageEffectEvent::new, WeightedDamageEffectEvent::entries).fieldOf("entries");

    @Override
    public void apply(DamageEffectContext context) {
        RandomSource source = context.target().getRandom();
        this.entries.getRandom(source).ifPresent(event -> event.apply(context));
    }

    @Override
    public void validate(DamageEffectContextType type) {
        this.entries.unwrap().forEach(weighted -> weighted.value().validate(type));
    }

    @Override
    public DamageEffectEventType<?> getType() {
        return MedSystemDamageEffectEvents.WEIGHTED.value();
    }
}
