package tnt.tarkovcraft.medsystem.common.health;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import tnt.tarkovcraft.core.network.message.S2C_SendDataAttachments;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class HealthSystem extends SimpleJsonResourceReloadListener<HealthContainerDefinition> {

    public static final Marker MARKER = MarkerManager.getMarker("HealthSystemManager");
    public static final ResourceLocation IDENTIFIER = MedicalSystem.resource("health_system");
    private final Map<EntityType<?>, HealthContainerDefinition> healthContainers = new HashMap<>();

    public HealthSystem() {
        super(HealthContainerDefinition.CODEC, FileToIdConverter.json("tarkovcraft/health"));
    }

    public static void synchronizeEntity(LivingEntity entity) {
        if (!entity.level().isClientSide())
            PacketDistributor.sendToPlayersTrackingEntity(entity, new S2C_SendDataAttachments(entity, MedSystemDataAttachments.HEALTH_CONTAINER.get()));
    }

    public Optional<HealthContainerDefinition> getHealthContainer(EntityType<?> type) {
        return Optional.ofNullable(this.healthContainers.get(type));
    }

    public Optional<HealthContainerDefinition> getHealthContainer(LivingEntity entity) {
        return this.getHealthContainer(entity.getType());
    }

    @Override
    protected void apply(Map<ResourceLocation, HealthContainerDefinition> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        MedicalSystem.LOGGER.debug(MARKER, "Loading custom entity health containers");
        this.healthContainers.clear();
        for (HealthContainerDefinition definition : map.values()) {
            List<EntityType<?>> types = definition.getTargets();
            types.forEach(type -> this.healthContainers.merge(type, definition, HealthContainerDefinition::merge));
        }
        MedicalSystem.LOGGER.debug(MARKER, "Loaded {} custom entity health containers", this.healthContainers.size());
    }
}
