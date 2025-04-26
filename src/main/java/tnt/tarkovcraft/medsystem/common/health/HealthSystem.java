package tnt.tarkovcraft.medsystem.common.health;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import tnt.tarkovcraft.core.network.message.S2C_SendDataAttachments;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.HitCalculatorResolveEvent;
import tnt.tarkovcraft.medsystem.api.event.HitboxPiercingEvent;
import tnt.tarkovcraft.medsystem.common.health.math.*;
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
        if (!entity.level().isClientSide()) {
            S2C_SendDataAttachments packet = new S2C_SendDataAttachments(entity, MedSystemDataAttachments.HEALTH_CONTAINER.get());
            PacketDistributor.sendToPlayersTrackingEntity(entity, packet);
            if (entity instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }

    public static HitCalculator getHitCalculator(LivingEntity entity, DamageSource source, HealthContainer container) {
        HitCalculator eventCalculator = NeoForge.EVENT_BUS.post(new HitCalculatorResolveEvent(entity, source, container)).getCalculator();
        if (eventCalculator != null) {
            return eventCalculator;
        }
        if (source.is(DamageTypeTags.IS_FALL)) {
            return FallDamageHitCalculator.INSTANCE;
        }
        // TODO explosives
        Entity sourceEntity = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
        if (sourceEntity == null) {
            return GenericHitCalculator.INSTANCE;
        }
        if (source.isDirect()) {
            return MeleeHitCalculator.INSTANCE;
        } else {
            return ProjectileHitCalculator.INSTANCE;
        }
    }

    public static int getProjectilePiercing(LivingEntity entity, DamageSource source, HealthContainer container, Entity projectile) {
        int pierceLevel = 1;
        if (projectile instanceof AbstractArrow arrow) {
            pierceLevel += arrow.getPierceLevel();
        }
        return NeoForge.EVENT_BUS.post(new HitboxPiercingEvent(entity, source, container, projectile, pierceLevel)).getPiercing();
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
