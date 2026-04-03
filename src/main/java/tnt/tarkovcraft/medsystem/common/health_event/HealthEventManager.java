package tnt.tarkovcraft.medsystem.common.health_event;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import tnt.tarkovcraft.medsystem.MedicalSystem;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;

public final class HealthEventManager extends SimpleJsonResourceReloadListener<HealthEvent> {

    public static final Marker MARKER = MarkerManager.getMarker("StatusEffectEventManager");
    public static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("status_effect_events");
    private final Multimap<HealthEventTriggerSource, NamedStatusEffectEvent> eventMappings = ArrayListMultimap.create();

    public HealthEventManager() {
        super(HealthEvent.CODEC, FileToIdConverter.json("tarkovcraft/status_effect_event"));
    }

    @Override
    protected void apply(Map<Identifier, HealthEvent> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        MedicalSystem.LOGGER.debug(MARKER, "Registering status effect events");
        this.eventMappings.clear();
        for (Map.Entry<Identifier, HealthEvent> entry : object.entrySet()) {
            Identifier identifier = entry.getKey();
            HealthEvent event = entry.getValue();
            // debug id so we can easily find out which events were actually registered
            MedicalSystem.LOGGER.debug(MARKER, "Registering '{}' status effect event for event source '{}'", identifier, event.eventSource().identifier());
            this.eventMappings.put(event.eventSource(), new NamedStatusEffectEvent(identifier, event));
        }
        MedicalSystem.LOGGER.info(MARKER, "Registered {} status effect events", this.eventMappings.size());
    }

    public void triggerEvent(Supplier<HealthEventTriggerSource> sourceSupplier, HealthEventContext context) {
        this.triggerEvent(sourceSupplier.get(), context);
    }

    public void triggerEvent(Holder<HealthEventTriggerSource> sourceHolder, HealthEventContext context) {
        this.triggerEvent(sourceHolder.value(), context);
    }

    public void triggerEvent(HealthEventTriggerSource source, HealthEventContext context) {
        Collection<NamedStatusEffectEvent> events = this.eventMappings.get(source);
        Iterator<NamedStatusEffectEvent> iterator = events.iterator();
        while (iterator.hasNext()) {
            NamedStatusEffectEvent event = iterator.next();
            HealthEventResult result = event.trigger(context);
            if (result == HealthEventResult.INVALID) {
                MedicalSystem.LOGGER.error(MARKER, "Removing invalid status effect event '{}' as it is not triggerable for event source {}", event.id, source.identifier());
                iterator.remove();
            }
        }
    }

    private record NamedStatusEffectEvent(Identifier id, HealthEvent event) {

        HealthEventResult trigger(HealthEventContext context) {
            return this.event.trigger(context);
        }
    }
}
