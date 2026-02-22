package tnt.tarkovcraft.medsystem.common.effect.event;

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

public final class StatusEffectEventManager extends SimpleJsonResourceReloadListener<StatusEffectEvent> {

    public static final Marker MARKER = MarkerManager.getMarker("StatusEffectEventManager");
    public static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("status_effect_events");
    private final Multimap<StatusEffectEventSource, NamedStatusEffectEvent> eventMappings = ArrayListMultimap.create();

    public StatusEffectEventManager() {
        super(StatusEffectEvent.CODEC, FileToIdConverter.json("tarkovcraft/status_effect_event"));
    }

    @Override
    protected void apply(Map<Identifier, StatusEffectEvent> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        MedicalSystem.LOGGER.debug(MARKER, "Registering status effect events");
        this.eventMappings.clear();
        for (Map.Entry<Identifier, StatusEffectEvent> entry : object.entrySet()) {
            Identifier identifier = entry.getKey();
            StatusEffectEvent event = entry.getValue();
            // debug id so we can easily find out which events were actually registered
            MedicalSystem.LOGGER.debug(MARKER, "Registering '{}' status effect event for event source '{}'", identifier, event.eventSource().identifier());
            this.eventMappings.put(event.eventSource(), new NamedStatusEffectEvent(identifier, event));
        }
        MedicalSystem.LOGGER.info(MARKER, "Registered {} status effect events", this.eventMappings.size());
    }

    public void triggerEvent(Supplier<StatusEffectEventSource> sourceSupplier, StatusEffectEventContext context) {
        this.triggerEvent(sourceSupplier.get(), context);
    }

    public void triggerEvent(Holder<StatusEffectEventSource> sourceHolder, StatusEffectEventContext context) {
        this.triggerEvent(sourceHolder.value(), context);
    }

    public void triggerEvent(StatusEffectEventSource source, StatusEffectEventContext context) {
        Collection<NamedStatusEffectEvent> events = this.eventMappings.get(source);
        Iterator<NamedStatusEffectEvent> iterator = events.iterator();
        while (iterator.hasNext()) {
            NamedStatusEffectEvent event = iterator.next();
            TriggerResult result = event.trigger(context);
            if (result == TriggerResult.INVALID) {
                MedicalSystem.LOGGER.error(MARKER, "Removing invalid status effect event '{}' as it is not triggerable for event source {}", event.id, source.identifier());
                iterator.remove();
            }
        }
    }

    private record NamedStatusEffectEvent(Identifier id, StatusEffectEvent event) {

        TriggerResult trigger(StatusEffectEventContext context) {
            return this.event.trigger(context);
        }
    }
}
