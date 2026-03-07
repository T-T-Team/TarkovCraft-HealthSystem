package tnt.tarkovcraft.medsystem.common.blood_system;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import tnt.tarkovcraft.medsystem.MedicalSystem;

import java.io.IOException;
import java.io.Reader;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public final class BloodSystemManager {

    private static final Marker MARKER = MarkerManager.getMarker("BloodSystemManager");
    private final ConfigurationLoader configLoader = new ConfigurationLoader();
    private final BloodAssignmentLoader assignmentLoader = new BloodAssignmentLoader();

    public void registerServerDataListeners(Consumer<PreparableReloadListener> registration) {
        registration.accept(this.configLoader);
    }

    public BloodConfiguration getConfig() {
        return this.configLoader.config;
    }

    private static final class ConfigurationLoader extends SimplePreparableReloadListener<BloodConfiguration> {

        private static final ResourceLocation ID = MedicalSystem.resource("blood_system/blood_configuration");
        private BloodConfiguration config = BloodConfiguration.missingConfiguration();

        @Override
        protected BloodConfiguration prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
            Optional<Resource> optional = resourceManager.getResource(MedicalSystem.resource("tarkovcraft/blood_system/blood_configuration.json"));
            Resource resource = optional.orElseThrow(() -> new IllegalStateException("Blood configuration file not found"));
            BloodConfiguration configuration;
            try (Reader reader = resource.openAsReader()) {
                DataResult<BloodConfiguration> result = BloodConfiguration.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader));
                configuration = result.getPartialOrThrow(err -> new IllegalStateException("Failed to parse blood config file: " + err));
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load blood configuration file", e);
            }
            return configuration;
        }

        @Override
        protected void apply(BloodConfiguration configuration, ResourceManager resourceManager, ProfilerFiller profiler) {
            this.config = configuration;
            MedicalSystem.LOGGER.info(MARKER, "Blood configuration loaded");
            if (MedicalSystem.LOGGER.isDebugEnabled(MARKER)) {
                Set<ResourceLocation> bloodTypes = this.config.bloodTypes().keySet();
                bloodTypes.forEach(id -> MedicalSystem.LOGGER.debug(MARKER, "Registered blood type: {}", id));
            }
        }
    }

    private static final class BloodAssignmentLoader {}
}
