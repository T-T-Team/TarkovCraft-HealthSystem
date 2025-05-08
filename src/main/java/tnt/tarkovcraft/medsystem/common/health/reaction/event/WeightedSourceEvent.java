package tnt.tarkovcraft.medsystem.common.health.reaction.event;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.common.health.reaction.HealthEventSource;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactionResponses;

public class WeightedSourceEvent implements HealthSourceEvent {

    public static final MapCodec<WeightedSourceEvent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            WeightedList.codec(HealthSourceEventType.CODEC).fieldOf("values").forGetter(t -> t.events)
    ).apply(instance, WeightedSourceEvent::new));

    private final WeightedList<HealthSourceEvent> events;

    public WeightedSourceEvent(WeightedList<HealthSourceEvent> events) {
        this.events = events;
    }

    @Override
    public void onReactionPassed(HealthEventSource source, Context context) {
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
         this.events.getRandom(entity.getRandom()).ifPresent(item -> item.onReactionPassed(source, context));
    }

    @Override
    public HealthSourceEventType<?> getType() {
        return MedSystemHealthReactionResponses.WEIGHTED.get();
    }
}
