package tnt.tarkovcraft.medsystem.common.health;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.HitboxPiercingEvent;
import tnt.tarkovcraft.medsystem.api.event.PainCheckEvent;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.calc.*;
import tnt.tarkovcraft.medsystem.common.health.distributor.PoisonDamageDistributor;
import tnt.tarkovcraft.medsystem.common.health.distributor.ScaledDamageDistributor;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTags;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodStatus;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;
import tnt.tarkovcraft.medsystem.network.message.S2C_SendHealthDefinitions;

import java.util.*;
import java.util.stream.Stream;

public final class HealthSystem extends SimpleJsonResourceReloadListener<HealthContainerDefinition> {

    public static final Marker MARKER = MarkerManager.getMarker("HealthSystemManager");
    public static final Identifier IDENTIFIER = MedicalSystem.createIdentifier("health_system");
    private final Map<EntityType<?>, HealthContainerDefinition> healthContainers = new HashMap<>();
    private final List<HitCalculatorRule> rules = new ArrayList<>();

    public HealthSystem() {
        super(HealthContainerDefinition.CODEC, FileToIdConverter.json("tarkovcraft/health"));

        this.registerHitCalculatorRule(new HitCalculatorRule(HitCalculatorRule.SPECIFIC_PART, SpecificBodyPartHitCalculator::canApply, SpecificBodyPartHitCalculator::createInstance));
        this.registerHitCalculatorRule(new HitCalculatorRule(HitCalculatorRule.ENVIRONMENT, FallDamageHitCalculator::isFall, ctx -> FallDamageHitCalculator.INSTANCE));
        this.registerHitCalculatorRule(new HitCalculatorRule(HitCalculatorRule.ENVIRONMENT, ExplosionHitCalculator::isExplosion, ctx -> ExplosionHitCalculator.INSTANCE));
        this.registerHitCalculatorRule(new HitCalculatorRule(HitCalculatorRule.ENVIRONMENT, LavaHitCalculator::canApply, ctx -> LavaHitCalculator.INSTANCE));
        this.registerHitCalculatorRule(new HitCalculatorRule(HitCalculatorRule.EFFECTS, MovementDamageHitCalculator::canApply, ctx -> MovementDamageHitCalculator.INSTANCE));
        this.registerHitCalculatorRule(new HitCalculatorRule(HitCalculatorRule.EFFECTS, ctx -> ctx.isDamage(NeoForgeMod.POISON_DAMAGE), ctx -> new DelegateHitCalculator(GenericHitCalculator.INSTANCE, PoisonDamageDistributor.INSTANCE)));
        this.registerHitCalculatorRule(new HitCalculatorRule(HitCalculatorRule.GENERIC, ctx -> ctx.getAttackingEntity() == null && ctx.isDamageType(MedSystemTags.DamageTypes.IS_GENERIC), ctx -> GenericHitCalculator.INSTANCE));
        this.registerHitCalculatorRule(new HitCalculatorRule(HitCalculatorRule.MELEE, ctx -> ctx.getAttackingEntity() != null && ctx.source().isDirect(), ctx -> MeleeHitCalculator.INSTANCE));
        this.registerHitCalculatorRule(new HitCalculatorRule(HitCalculatorRule.PROJECTILE, ctx -> ctx.getProjectile() != null, ctx -> ProjectileHitCalculator.DEFAULT));
    }

    public synchronized void registerHitCalculatorRule(HitCalculatorRule rule) {
        this.rules.add(rule);
        this.rules.sort(Comparator.comparingInt(HitCalculatorRule::priority));
    }

    public static boolean hasCustomHealth(Entity entity) {
        return entity.hasData(MedSystemDataAttachments.HEALTH_CONTAINER);
    }

    public static HealthContainer getHealthData(LivingEntity entity) {
        return entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
    }

    public static HealthContainer getHealthDataOrThrow(LivingEntity entity) {
        return Objects.requireNonNull(getHealthData(entity), String.format(Locale.ROOT, "Entity '%s' does not have health data attached", entity));
    }

    public static boolean hasPainRelief(LivingEntity entity) {
        if (!hasCustomHealth(entity))
            return false;
        HealthContainer container = getHealthData(entity);
        return container.getGlobalStatusEffects().hasEffect(MedSystemTags.StatusEffects.IS_PAIN_RELIEF);
    }

    public static boolean isInPain(LivingEntity entity) {
        if (!hasCustomHealth(entity) || hasPainRelief(entity))
            return false;
        HealthContainer container = getHealthData(entity);
        boolean inPain = container.getStatusEffectStream().anyMatch(effect -> effect.getType().is(MedSystemTags.StatusEffects.IS_PAIN_CAUSING));
        if (BloodSystem.hasBloodDataIntegration(entity)) {
            BloodData data = BloodSystem.getBloodData(entity);
            BloodStatus status = BloodStatus.fromBloodLevelPercentage(data.getBloodVolumePercentage());
            if (status.isSameOrBelow(BloodStatus.MODERATE_BLOOD_LOSS)) {
                inPain = true;
            }
        }
        PainCheckEvent event = NeoForge.EVENT_BUS.post(new PainCheckEvent(entity, container, inPain));
        return event.isInPain();
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
        if (BloodSystem.hasBloodDataIntegration(entity)) {
            BloodData data = BloodSystem.getBloodData(entity);
            BloodStatus status = BloodStatus.fromBloodLevelPercentage(data.getBloodVolumePercentage());
            if (status.isSameOrBelow(BloodStatus.MODERATE_BLOOD_LOSS)) {
                return true;
            }
        }
        Stream<Limb> parts = healthContainer.getLimbsAsStream();
        return parts.anyMatch(HealthSystem::isMovementRestrictedOnLimb);
    }

    public static boolean isMovementRestrictedOnLimb(Limb part) {
        return part.getType() == LimbType.LEG && (part.isDead() || part.getStatusEffects().hasEffect(MedSystemTags.StatusEffects.MOVEMENT_RESTRICTING));
    }

    public static void synchronizeEntity(LivingEntity entity) {
        if (!entity.level().isClientSide() && hasCustomHealth(entity)) {
            entity.syncData(MedSystemDataAttachments.HEALTH_CONTAINER);
        }
    }

    public HitCalculator getHitCalculator(HitCalculationContext context) {
        for (HitCalculatorRule rule : this.rules) {
            if (rule.validate(context)) {
                return rule.createCalculator(context);
            }
        }
        return GenericHitCalculator.INSTANCE;
    }

    public static int getProjectilePiercing(HitCalculationContext context) {
        int pierceLevel = 1;
        if (context.getProjectile() instanceof AbstractArrow arrow) {
            pierceLevel += arrow.getPierceLevel();
        }
        return NeoForge.EVENT_BUS.post(new HitboxPiercingEvent(context, pierceLevel)).getPiercing();
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
    protected void apply(Map<Identifier, HealthContainerDefinition> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        MedicalSystem.LOGGER.debug(MARKER, "Loading custom entity health containers");
        this.healthContainers.clear();
        for (HealthContainerDefinition definition : map.values()) {
            List<EntityType<?>> targets = definition.targets();
            targets.forEach(type -> {
                if (this.healthContainers.put(type, definition) != null) {
                    MedicalSystem.LOGGER.warn(MARKER, "Detected health container override for entity {}", EntityType.getKey(type));
                }
            });
        }
        MedicalSystem.LOGGER.debug(MARKER, "Loaded {} custom entity health containers", this.healthContainers.size());
    }
}
