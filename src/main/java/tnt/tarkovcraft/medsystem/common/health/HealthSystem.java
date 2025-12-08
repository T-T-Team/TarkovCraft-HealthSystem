package tnt.tarkovcraft.medsystem.common.health;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.SpecificBodyPartDamage;
import tnt.tarkovcraft.medsystem.api.event.HitCalculatorResolveEvent;
import tnt.tarkovcraft.medsystem.api.event.HitboxPiercingEvent;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.math.*;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTags;
import tnt.tarkovcraft.medsystem.network.message.S2C_SendHealthDefinitions;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public final class HealthSystem extends SimpleJsonResourceReloadListener {

    public static final Marker MARKER = MarkerManager.getMarker("HealthSystemManager");
    public static final ResourceLocation IDENTIFIER = MedicalSystem.resource("health_system");
    private final Map<EntityType<?>, HealthContainerDefinition> healthContainers = new HashMap<>();

    public HealthSystem() {
        super(new Gson(), "tarkovcraft/health");
    }

    public static boolean hasCustomHealth(Entity entity) {
        return entity.hasData(MedSystemDataAttachments.HEALTH_CONTAINER);
    }

    public static HealthContainer getHealthData(LivingEntity entity) {
        return entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
    }

    public static boolean hasPainRelief(LivingEntity entity) {
        if (!hasCustomHealth(entity))
            return false;
        HealthContainer container = getHealthData(entity);
        return container.getGlobalStatusEffects().hasEffect(MedSystemTags.StatusEffects.IS_PAIN_RELIEF);
    }

    public static boolean isInPain(LivingEntity entity) {
        return hasCustomHealth(entity) && !hasPainRelief(entity) && getHealthData(entity).getStatusEffectStream()
                .anyMatch(effect -> effect.getType().is(MedSystemTags.StatusEffects.IS_PAIN_CAUSING));
    }

    public static boolean isMovementRestricted(LivingEntity entity) {
        if (!hasCustomHealth(entity))
            return false;
        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }
        HealthContainer healthContainer = getHealthData(entity);
        StatusEffectMap map = healthContainer.getGlobalStatusEffects();
        if (map.hasEffect(MedSystemTags.StatusEffects.MOVEMENT_RESTRICTING)) {
            return true;
        }
        Stream<Limb> parts = healthContainer.getLimbsAsStream();
        return parts.anyMatch(HealthSystem::isMovementRestrictingPart);
    }

    public static boolean isMovementRestrictingPart(Limb part) {
        return part.getType() == LimbType.LEG && (part.isDead() || part.getStatusEffects().hasEffect(MedSystemTags.StatusEffects.MOVEMENT_RESTRICTING));
    }

    public static void synchronizeEntity(LivingEntity entity) {
        if (!entity.level().isClientSide() && hasCustomHealth(entity)) {
            entity.syncData(MedSystemDataAttachments.HEALTH_CONTAINER);
        }
    }

    public static HitCalculator getHitCalculator(LivingEntity entity, DamageSource source, HealthContainer container) {
        HitCalculator eventCalculator = NeoForge.EVENT_BUS.post(new HitCalculatorResolveEvent(entity, source, container)).getCalculator();
        if (eventCalculator != null) {
            return eventCalculator;
        }
        if (source instanceof SpecificBodyPartDamage bodyPartDamage) {
            return new SpecificBodyPartHitCalculator(bodyPartDamage.getBodyParts(), bodyPartDamage.allowDeadBodyPartDamage());
        }
        if (source.is(DamageTypeTags.IS_FALL)) {
            return FallDamageHitCalculator.INSTANCE;
        }
        if (ExplosionHitCalculator.isValidExplosionSource(source)) {
            return ExplosionHitCalculator.INSTANCE;
        }
        if (source == entity.damageSources().lava()) {
            return LavaHitCalculator.INSTANCE;
        }
        if (source.is(MedSystemTags.DamageTypes.IS_MOVEMENT_RESTRICTED)) {
            return MovementDamageHitCalculator.INSTANCE;
        }
        Entity sourceEntity = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
        if (sourceEntity == null || source.is(MedSystemTags.DamageTypes.IS_GENERIC)) {
            return GenericHitCalculator.INSTANCE;
        }
        if (source.isDirect()) {
            return MeleeHitCalculator.INSTANCE;
        } else if (source.getDirectEntity() != null) {
            return ProjectileHitCalculator.INSTANCE;
        } else {
            return GenericHitCalculator.INSTANCE;
        }
    }

    public static int getProjectilePiercing(LivingEntity entity, DamageSource source, HealthContainer container, Entity projectile) {
        int pierceLevel = 1;
        if (projectile instanceof AbstractArrow arrow) {
            pierceLevel += arrow.getPierceLevel();
        }
        return NeoForge.EVENT_BUS.post(new HitboxPiercingEvent(entity, source, container, projectile, pierceLevel)).getPiercing();
    }

    public static List<HitResult> getClosestPossibleHits(Vec3 point, LivingEntity entity, HealthContainer container, BiPredicate<BodyPartHitbox, Limb> filter) {
        List<HitResult> results = new ArrayList<>();
        container.acceptHitboxes(
                filter,
                (hitbox, part) -> {
                    AABB aabb = hitbox.getLevelPositionedAABB(entity);
                    Vec3 aabbCenter = aabb.getCenter();
                    results.add(new HitResult(hitbox, part, aabb, aabbCenter));
                }
        );
        results.sort(Comparator
                .<HitResult>comparingDouble(res -> res.aabb().getCenter().y - point.y)
                .thenComparingDouble(res -> res.aabb().getCenter().distanceToSqr(point))
        );
        return results;
    }

    public Optional<HealthContainerDefinition> getHealthContainer(EntityType<?> type) {
        return Optional.ofNullable(this.healthContainers.get(type));
    }

    public Optional<HealthContainerDefinition> getHealthContainer(LivingEntity entity) {
        return this.getHealthContainer(entity.getType());
    }

    public void importServerData(Map<EntityType<?>, HealthContainerDefinition> data) {
        MedicalSystem.LOGGER.debug(MARKER, "Importing server data, total of {} entries", data.size());
        this.healthContainers.clear();
        this.healthContainers.putAll(data);
    }

    public CustomPacketPayload getConfigurationPayload() {
        return new S2C_SendHealthDefinitions(this.healthContainers);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        MedicalSystem.LOGGER.debug(MARKER, "Loading custom entity health containers");
        this.healthContainers.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            try {
                DataResult<HealthContainerDefinition> result = HealthContainerDefinition.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
                HealthContainerDefinition definition = result.getOrThrow();
                List<EntityType<?>> targets = definition.getTargets();
                targets.forEach(type -> {
                    if (this.healthContainers.put(type, definition) != null) {
                        MedicalSystem.LOGGER.warn(MARKER, "Detected health container override for entity {}", EntityType.getKey(type));
                    }
                });
            } catch (Exception e) {
                MedicalSystem.LOGGER.error(MARKER, "Failed to load health container file {} due to error", entry.getKey(), e);
            }
        }
        MedicalSystem.LOGGER.debug(MARKER, "Loaded {} custom entity health containers", this.healthContainers.size());
    }
}
