package tnt.tarkovcraft.medsystem.common.effect.event;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
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

public final class StatusEffectEventManager extends SimpleJsonResourceReloadListener {

    public static final Marker MARKER = MarkerManager.getMarker("StatusEffectEventManager");
    public static final ResourceLocation IDENTIFIER = MedicalSystem.resource("status_effect_events");
    private final Multimap<StatusEffectEventSource, NamedStatusEffectEvent> eventMappings = ArrayListMultimap.create();

    public StatusEffectEventManager() {
        super(new Gson(), "tarkovcraft/status_effect_event");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        MedicalSystem.LOGGER.debug(MARKER, "Registering status effect events");
        this.eventMappings.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation identifier = entry.getKey();
            JsonElement element = entry.getValue();
            try {
                DataResult<StatusEffectEvent> result = StatusEffectEvent.CODEC.parse(JsonOps.INSTANCE, element);
                StatusEffectEvent effectEvent = result.getOrThrow();
                // debug id so we can easily find out which events were actually registered
                MedicalSystem.LOGGER.debug(MARKER, "Registering '{}' status effect event for event source '{}'", identifier, effectEvent.eventSource().identifier());
                this.eventMappings.put(effectEvent.eventSource(), new NamedStatusEffectEvent(identifier, effectEvent));
            } catch (Exception e) {
                MedicalSystem.LOGGER.error(MARKER, "Failed to parse status effect event '{}'", identifier, e);
            }
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

    private record NamedStatusEffectEvent(ResourceLocation id, StatusEffectEvent event) {
        TriggerResult trigger(StatusEffectEventContext context) {
            return this.event.trigger(context);
        }
    }
}
