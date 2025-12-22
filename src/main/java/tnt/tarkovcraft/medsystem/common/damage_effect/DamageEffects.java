package tnt.tarkovcraft.medsystem.common.damage_effect;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import tnt.tarkovcraft.medsystem.MedicalSystem;

import java.util.Map;
import java.util.function.Consumer;

public final class DamageEffects extends SimpleJsonResourceReloadListener {

    public static final Marker MARKER = MarkerManager.getMarker("DamageEffects");
    private final Multimap<DamageEffectContextType, DamageEffect> registeredEffects = ArrayListMultimap.create();

    public DamageEffects() {
        super(new Gson(), "tarkovcraft/damage_effect");
    }

    public void apply(DamageEffectContextType type, Consumer<DamageEffect> action) {
        this.registeredEffects.get(type).forEach(action);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        MedicalSystem.LOGGER.debug(MARKER, "Loading custom damage effects");
        this.registeredEffects.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            try {
                DataResult<DamageEffect> result = DamageEffect.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
                DamageEffect effect = result.getOrThrow();
                this.registeredEffects.put(effect.contextType(), effect);
            } catch (Exception e) {
                MedicalSystem.LOGGER.error(MARKER, "Failed to load damage effect {}", id, e);
            }
        }
        MedicalSystem.LOGGER.debug(MARKER, "Loaded {} custom damage effects", this.registeredEffects.size());
    }
}
