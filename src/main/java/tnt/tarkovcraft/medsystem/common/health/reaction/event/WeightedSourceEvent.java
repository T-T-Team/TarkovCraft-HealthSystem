package tnt.tarkovcraft.medsystem.common.health.reaction.event;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.reaction.HealthEventSource;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactionResponses;

import javax.annotation.Nullable;

public class WeightedSourceEvent implements HealthSourceEvent {

    public static final MapCodec<WeightedSourceEvent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            WeightedList.codec(HealthSourceEventType.CODEC).fieldOf("values").forGetter(t -> t.events)
    ).apply(instance, WeightedSourceEvent::new));

    private final WeightedList<HealthSourceEvent> events;

    public WeightedSourceEvent(WeightedList<HealthSourceEvent> events) {
        this.events = events;
    }

    @Override
    public void onReactionPassed(HealthEventSource source, HealthContainer container, LivingEntity entity, @Nullable DamageSource damageSource, Limb limb) {
         this.events.getRandom(entity.getRandom()).ifPresent(item -> item.onReactionPassed(source, container, entity, damageSource, limb));
    }

    @Override
    public HealthSourceEventType<?> getType() {
        return MedSystemHealthReactionResponses.WEIGHTED.get();
    }
}
