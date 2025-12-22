package tnt.tarkovcraft.medsystem.common.damage_effect;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import tnt.tarkovcraft.medsystem.MedicalSystem;

import java.util.Map;
import java.util.function.Consumer;

public class DamageEffects extends SimpleJsonResourceReloadListener<DamageEffect> {

    public static final Marker MARKER = MarkerManager.getMarker("DamageEffects");
    public static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("damage_effects");
    private final Multimap<DamageEffectContextType, DamageEffect> registeredEffects = ArrayListMultimap.create();

    public DamageEffects() {
        super(DamageEffect.CODEC, FileToIdConverter.json("tarkovcraft/damage_effect"));
    }

    public void apply(DamageEffectContextType type, Consumer<DamageEffect> action) {
        this.registeredEffects.get(type).forEach(action);
    }

    @Override
    protected void apply(Map<Identifier, DamageEffect> identifierDamageEffectMap, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        MedicalSystem.LOGGER.debug(MARKER, "Loading custom damage effects");
        this.registeredEffects.clear();
        identifierDamageEffectMap.values().forEach(damageEffect -> {
            DamageEffectContextType contextType = damageEffect.contextType();
            this.registeredEffects.put(contextType, damageEffect);
        });
        MedicalSystem.LOGGER.debug(MARKER, "Loaded {} custom damage effects", this.registeredEffects.size());
    }
}
