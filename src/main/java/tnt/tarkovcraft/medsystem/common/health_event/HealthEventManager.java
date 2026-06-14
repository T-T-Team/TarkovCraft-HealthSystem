package tnt.tarkovcraft.medsystem.common.health_event;

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

public final class HealthEventManager extends SimpleJsonResourceReloadListener {

    public static final Marker MARKER = MarkerManager.getMarker("StatusEffectEventManager");
    public static final ResourceLocation IDENTIFIER = MedicalSystem.createIdentifier("health_event");
    private final Multimap<HealthEventTriggerSource, NamedStatusEffectEvent> eventMappings = ArrayListMultimap.create();

    public HealthEventManager() {
        super(new Gson(), "tarkovcraft/health_event");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        MedicalSystem.LOGGER.debug(MARKER, "Registering status effect events");
        this.eventMappings.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation identifier = entry.getKey();
            JsonElement element = entry.getValue();
            try {
                DataResult<HealthEvent> result = HealthEvent.CODEC.parse(JsonOps.INSTANCE, element);
                HealthEvent effectEvent = result.getOrThrow();
                // debug id so we can easily find out which events were actually registered
                MedicalSystem.LOGGER.debug(MARKER, "Registering '{}' status effect event for event source '{}'", identifier, effectEvent.eventSource().identifier());
                this.eventMappings.put(effectEvent.eventSource(), new NamedStatusEffectEvent(identifier, effectEvent));
            } catch (Exception e) {
                MedicalSystem.LOGGER.error(MARKER, "Failed to parse status effect event '{}'", identifier, e);
            }
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

    private record NamedStatusEffectEvent(ResourceLocation id, HealthEvent event) {
        HealthEventResult trigger(HealthEventContext context) {
            return this.event.trigger(context);
        }
    }
}
