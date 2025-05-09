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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.compatibility.Component;
import tnt.tarkovcraft.core.network.message.S2C_SendDataAttachments;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.ArmorComponent;
import tnt.tarkovcraft.medsystem.api.SpecificBodyPartDamage;
import tnt.tarkovcraft.medsystem.api.event.HitCalculatorResolveEvent;
import tnt.tarkovcraft.medsystem.api.event.HitboxPiercingEvent;
import tnt.tarkovcraft.medsystem.common.health.math.*;
import tnt.tarkovcraft.medsystem.common.init.MedSystemAttributes;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffects;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTags;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

public final class HealthSystem extends SimpleJsonResourceReloadListener<HealthContainerDefinition> {

    public static final Component<ArmorComponent> ARMOR = new Component<>("armor", DefaultArmorComponent.INSTANCE);

    public static final Marker MARKER = MarkerManager.getMarker("HealthSystemManager");
    public static final ResourceLocation IDENTIFIER = MedicalSystem.resource("health_system");
    private final Map<EntityType<?>, HealthContainerDefinition> healthContainers = new HashMap<>();

    public HealthSystem() {
        super(HealthContainerDefinition.CODEC, FileToIdConverter.json("tarkovcraft/health"));
    }

    public static boolean hasCustomHealth(Entity entity) {
        return entity.getType() == EntityType.PLAYER || (entity instanceof LivingEntity && entity.hasData(MedSystemDataAttachments.HEALTH_CONTAINER));
    }

    public static HealthContainer getHealthData(LivingEntity entity) {
        return entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
    }

    public static boolean hasPainRelief(LivingEntity entity) {
        return AttributeSystem.getIntValue(entity, MedSystemAttributes.PAIN_RELIEF, 0) > 0;
    }

    public static boolean isMovementRestricted(LivingEntity entity) {
        if (!hasCustomHealth(entity))
            return false;
        HealthContainer healthContainer = getHealthData(entity);
        Stream<BodyPart> parts = healthContainer.getBodyPartStream();
        return parts.anyMatch(HealthSystem::isMovementRestrictingPart);
    }

    public static boolean isMovementRestrictingPart(BodyPart part) {
        return part.getGroup() == BodyPartGroup.LEG && (part.isDead() || part.getStatusEffects().hasEffect(MedSystemStatusEffects.FRACTURE));
    }

    public static void synchronizeEntity(LivingEntity entity) {
        if (!entity.level().isClientSide() && hasCustomHealth(entity)) {
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

    public static List<HitResult> getClosestPossibleHits(Vec3 point, LivingEntity entity, HealthContainer container, BiPredicate<BodyPartHitbox, BodyPart> filter) {
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

    @Override
    protected void apply(Map<ResourceLocation, HealthContainerDefinition> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        MedicalSystem.LOGGER.debug(MARKER, "Loading custom entity health containers");
        this.healthContainers.clear();
        for (HealthContainerDefinition definition : map.values()) {
            List<EntityType<?>> targets = definition.getTargets();
            targets.forEach(type -> this.healthContainers.merge(type, definition, (d0, d1) -> HealthContainerHelper.merge(type, d0, d1)));
        }
        MedicalSystem.LOGGER.debug(MARKER, "Loaded {} custom entity health containers", this.healthContainers.size());
    }
}
