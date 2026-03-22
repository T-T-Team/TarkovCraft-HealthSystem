package tnt.tarkovcraft.medsystem.common.blood_system;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.core.util.helper.EntityHelper;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public final class BloodSystemManager {

    private static final Marker MARKER = MarkerManager.getMarker("BloodSystemManager");
    private final ConfigurationLoader configLoader = new ConfigurationLoader();
    private final BloodAssignmentLoader assignmentLoader = new BloodAssignmentLoader();

    public static boolean isEnabled() {
        MedSystemConfig config = MedicalSystem.getConfig();
        return config.bloodSystem.useBloodSystem;
    }

    public static boolean isEnabled(LivingEntity entity) {
        return isEnabled() && EntityBloodSystem.getAttached(entity) != null;
    }

    public static boolean isUnconscious(LivingEntity entity) {
        return isEnabled(entity) && EntityBloodSystem.getAttached(entity).isUnconscious();
    }

    public static void synchronize(LivingEntity entity) {
        EntityBloodSystem system = EntityBloodSystem.getAttached(entity);
        if (system != null) {
            system.synchronizeImmediately(entity);
        }
    }

    public static void handleNewEntity(LivingEntity entity) {
        if (isEnabled()) {
            EntityBloodSystem existingBloodSystem = EntityBloodSystem.getAttached(entity);
            if (existingBloodSystem != null && existingBloodSystem.isValidBloodAttachment(entity)) {
                EntityBloodSystemDefinition definition = EntityBloodSystemDefinition.forEntity(entity);
                definition.bindListeners(existingBloodSystem, entity);
                return;
            }
            EntityBloodSystem.detach(entity);
            EntityBloodSystemDefinition definition = EntityBloodSystemDefinition.forEntity(entity);
            if (definition != null) {
                definition.bind(entity);
            }
        } else {
            EntityBloodSystem.detach(entity);
        }
    }

    public static boolean canSkipUnconsciousMode(LivingEntity entity) {
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(entity);
        if (bloodSystem == null)
            return false;
        UnconsciousOptions options = bloodSystem.getActiveUnconsciousModeOptions();
        return !bloodSystem.hasBledOut() && bloodSystem.isUnconscious() && options.allowSkip() && !EntityHelper.isCreativeOrSpectator(entity);
    }

    public static boolean causeBloodLoss(LivingEntity entity, float amount) {
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(entity);
        if (bloodSystem != null && !EntityHelper.isCreativeOrSpectator(entity)) {
            bloodSystem.causeBloodLoss(amount);
            bloodSystem.synchronizeImmediately(entity);
            return true;
        }
        return false;
    }

    public void registerServerDataListeners(AddReloadListenerEvent event) {
        event.addListener(this.configLoader);
        event.addListener(this.assignmentLoader);
    }

    public BloodConfiguration getConfig() {
        return this.configLoader.config;
    }

    public EntityBloodSystemDefinition getAssignment(EntityType<?> type) {
        return this.assignmentLoader.assignmentMap.get(type);
    }

    public Set<ResourceLocation> getAvailableBloodTypes() {
        return this.getConfig().bloodTypes().keySet();
    }

    public NetworkData prepareSynchronizationData() {
        return new NetworkData(this.configLoader.config, this.assignmentLoader.assignmentMap);
    }

    public void receiveServerData(NetworkData networkData) {
        this.configLoader.importServerConfiguration(networkData.configuration());
        this.assignmentLoader.importServerAssignments(networkData.assignments());
    }

    public record NetworkData(BloodConfiguration configuration, Map<EntityType<?>, EntityBloodSystemDefinition> assignments) {

        public static final Codec<NetworkData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BloodConfiguration.CODEC.fieldOf("configuration").forGetter(NetworkData::configuration),
                Codec.unboundedMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), EntityBloodSystemDefinition.CODEC).fieldOf("assignments").forGetter(NetworkData::assignments)
        ).apply(instance, NetworkData::new));
        public static final StreamCodec<FriendlyByteBuf, NetworkData> STREAM_CODEC = StreamCodec.of(
                (buffer, value) -> buffer.writeNbt(Codecs.encodeWithCodec(CODEC, value)),
                buffer -> Codecs.decodeWithCodec(CODEC, buffer.readNbt())
        );
    }

    private static final class ConfigurationLoader extends SimplePreparableReloadListener<BloodConfiguration> {

        private static final ResourceLocation ID = MedicalSystem.resource("blood_configuration");
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

        void importServerConfiguration(BloodConfiguration configuration) {
            this.config = configuration;
        }
    }

    private final class BloodAssignmentLoader extends SimpleJsonResourceReloadListener {

        private static final ResourceLocation ID = MedicalSystem.resource("blood_assignment");
        private final Map<EntityType<?>, EntityBloodSystemDefinition> assignmentMap = new HashMap<>();

        private BloodAssignmentLoader() {
            super(new Gson(), "tarkovcraft/blood_system/entity");
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
            MedicalSystem.LOGGER.debug(MARKER, "Loading entity blood assignment data");
            this.assignmentMap.clear();
            Set<ResourceLocation> bloodTypeRegistry = BloodSystemManager.this.getAvailableBloodTypes();
            for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
                ResourceLocation id = entry.getKey();
                JsonElement json = entry.getValue();
                try {
                    DataResult<EntityBloodSystemDefinition> dataResult = EntityBloodSystemDefinition.CODEC.parse(JsonOps.INSTANCE, json);
                    EntityBloodSystemDefinition definition = dataResult.getOrThrow();
                    this.validateAndRegister(id, definition, bloodTypeRegistry);
                } catch (Exception e) {
                    MedicalSystem.LOGGER.error(MARKER, "Failed to load entity blood assignment file {} due to error", id, e);
                }
            }
            MedicalSystem.LOGGER.info(MARKER, "Loaded {} entity blood assignments", this.assignmentMap.size());
        }

        private void validateAndRegister(ResourceLocation fileId, EntityBloodSystemDefinition definition, Set<ResourceLocation> bloodTypeRegistry) {
            List<ResourceLocation> definedBloodTypes = definition.getAvailableBloodTypes();
            for (ResourceLocation bloodType : definedBloodTypes) {
                if (!bloodTypeRegistry.contains(bloodType)) {
                    MedicalSystem.LOGGER.error(MARKER, "Unknown blood type '{}' defined within file '{}'", bloodType, fileId);
                    return;
                }
            }
            Collection<EntityType<?>> entities = definition.getEntityTypes();
            for (EntityType<?> entity : entities) {
                if (this.assignmentMap.put(entity, definition) != null) {
                    MedicalSystem.LOGGER.warn(MARKER, "Detected blood assignment override for entity '{}' from '{}'", entity, fileId);
                }
            }
        }

        void importServerAssignments(Map<EntityType<?>, EntityBloodSystemDefinition> data) {
            this.assignmentMap.clear();
            this.assignmentMap.putAll(data);
        }
    }
}
