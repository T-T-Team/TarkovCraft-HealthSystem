package tnt.tarkovcraft.medsystem.common;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import tnt.tarkovcraft.core.api.MovementStaminaComponent;
import tnt.tarkovcraft.core.api.event.EntityWeightUpdateEvent;
import tnt.tarkovcraft.core.api.event.StaminaEvent;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.common.energy.EnergySystem;
import tnt.tarkovcraft.core.common.skill.SkillSystem;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.ArmorComponent;
import tnt.tarkovcraft.medsystem.api.event.WoundStatusEffectApplyEvent;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.effect.OverweightStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.WoundStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectSubmitter;
import tnt.tarkovcraft.medsystem.common.health.*;
import tnt.tarkovcraft.medsystem.common.health.math.DamageDistributor;
import tnt.tarkovcraft.medsystem.common.health.math.HitCalculator;
import tnt.tarkovcraft.medsystem.common.init.*;
import tnt.tarkovcraft.medsystem.common.item.InteractionTarget;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodStatus;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class MedicalSystemEventHandler {

    @SubscribeEvent
    private void onEntitySpawn(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (event.isCanceled())
            return;
        if (entity instanceof LivingEntity livingEntity) {
            MedicalSystem.HEALTH_SYSTEM.getHealthContainer(livingEntity).ifPresent(container -> {
                container.bind(livingEntity);
                HealthSystem.synchronizeEntity(livingEntity);
            });
            if (livingEntity.getType() == EntityType.PLAYER && !BloodSystem.hasBloodDataIntegration(livingEntity)) {
                livingEntity.setData(MedSystemDataAttachments.BLOOD_DATA, new BloodData(5.0F));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    private void onLivingHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        float amount = event.getAmount();
        if (event.isCanceled())
            return;
        if (amount > 0.0F && HealthSystem.hasCustomHealth(entity)) {
            float leftover = entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER).heal(entity, amount, null);
            if (leftover > 0.0F) {
                event.setAmount(amount - leftover);
            }
            HealthSystem.synchronizeEntity(entity);
        }
    }

    // Hitbox collision detection
    @SubscribeEvent
    private void onInvulnerabilityCheck(EntityInvulnerabilityCheckEvent event) {
        if (event.isInvulnerable())
            return;

        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity livingEntity))
            return;
        if (!HealthSystem.hasCustomHealth(livingEntity))
            return;

        HealthContainer container = entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        DamageSource source = event.getSource();

        // no in-block damage when unconscious
        if (BloodSystem.isEntityUnconscious(livingEntity) && source.is(DamageTypes.IN_WALL)) {
            event.setInvulnerable(true);
            return;
        }

        HitCalculator hitCalculator = HealthSystem.getHitCalculator(livingEntity, source, container);
        List<HitResult> hits = hitCalculator.calculateHits(livingEntity, source, container);
        if (hits == null || hits.isEmpty()) {
            event.setInvulnerable(true);
        } else {
            DamageContext context = new DamageContext(livingEntity, source);
            context.setHits(hits);
            context.setHitCalculator(hitCalculator);
            context.setSideEffects(SideEffectHolder.fromDamage(source));
            container.setDamageContext(context);
        }
    }

    // Armor damaging
    @SubscribeEvent
    private void onArmorHit(ArmorHurtEvent event) {
        if (event.isCanceled())
            return;
        LivingEntity entity = event.getEntity();
        ArmorComponent component = HealthSystem.ARMOR.getComponent();
        if (!HealthSystem.hasCustomHealth(entity) || component.useVanillaArmorDamage())
            return;
        HealthContainer container = entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        DamageContext context = container.getDamageContext();
        Set<EquipmentSlot> hitSlots = new HashSet<>(context.getAffectedSlots());
        Set<EquipmentSlot> armorSlots = new HashSet<>(event.getArmorMap().keySet());
        Map<EquipmentSlot, ArmorHurtEvent.ArmorEntry> map = event.getArmorMap();
        float damageReductionMultiplier = AttributeSystem.getFloatValue(entity, MedSystemAttributes.ARMOR_DURABILITY, 1.0F);
        for (EquipmentSlot slot : armorSlots) {
            if (!hitSlots.contains(slot)) {
                map.remove(slot);
            } else {
                float damage = event.getNewDamage(slot);
                if (damage > 0 && damageReductionMultiplier != 1.0F) {
                    event.setNewDamage(slot, Math.max(damage * damageReductionMultiplier, 1.0F));
                }
            }
        }
    }

    // Entity armor damage recalculation
    @SubscribeEvent
    private void onLivingDamage(LivingIncomingDamageEvent event) {
        // calculate correct damage for armor and so on
        LivingEntity entity = event.getEntity();
        if (!HealthSystem.hasCustomHealth(entity))
            return;

        HealthContainer container = entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        DamageContext context = container.getDamageContext();
        List<HitResult> hits = context.getHits();
        // Hit hitbox groups
        EnumSet<BodyPartGroup> hitGroups = EnumSet.noneOf(BodyPartGroup.class);
        for (HitResult hit : hits) {
            BodyPart bodyPart = hit.bodyPart();
            BodyPartGroup group = bodyPart.getGroup();
            hitGroups.add(group);
        }
        ArmorComponent component = HealthSystem.ARMOR.getComponent();
        // Protected hitbox groups
        EnumSet<BodyPartGroup> protectedGroups = EnumSet.noneOf(BodyPartGroup.class);
        component.collectAffectedBodyPartsWithProtection(
                protectedGroups::add,
                entity,
                context
        );
        // remove not affected groups
        protectedGroups.removeIf(group -> !hitGroups.contains(group));
        // armor reduction calculation preparation

        Set<EquipmentSlot> protectedSlots = protectedGroups.stream()
                .flatMap(group -> group.getArmorSlots().stream())
                .collect(Collectors.toSet());

        context.setAffectedSlots(new ArrayList<>());
        float reduction = component.handleReductions(
                entity,
                context,
                protectedSlots,
                event::getAmount,
                event::setAmount,
                event::addReductionModifier
        );
        if (reduction > 0.0F) {
            SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.ARMOR_USE, entity, reduction);
        }
    }

    // Entity damage application
    @SubscribeEvent
    private void onLivingApplyDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (!HealthSystem.hasCustomHealth(entity))
            return;
        HealthContainer container = entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        DamageSource source = event.getSource();
        DamageContext context = container.getDamageContext();
        DamageDistributor damageDistributor = context.getDamageDistributor(container);
        Map<BodyPart, Float> distributedDamage = damageDistributor.distribute(context, container, event.getNewDamage());
        List<BodyPart> lostBodyParts = new ArrayList<>();
        SideEffectHolder sideEffects = context.getSideEffects();

        // apply health container damage
        container.hurt(context, distributedDamage, sideEffects, lostBodyParts::add);

        // ignore skill leveling from /kill commands and other invulnerability bypassing effects - could be problematic for
        // specific projectile damage sources... maybe instead the max per-event progress amount should be limited
        float totalDamage = distributedDamage.values().stream().reduce(0.0F, Float::sum);
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.DAMAGE_TAKEN, entity, totalDamage);
        }

        // Apply wound status effect
        int duration = Mth.floor(totalDamage / 4.0F); // 4hp damage = 1s of wound status effect
        WoundStatusEffectApplyEvent applyEvent = NeoForge.EVENT_BUS.post(new WoundStatusEffectApplyEvent(entity, context, totalDamage, duration));
        if (applyEvent.shouldApplyEffect()) {
            StatusEffectHelper.addEffect(container.getGlobalStatusEffects(), entity, null, 1, new WoundStatusEffect(applyEvent.getDurationSeconds() * 20));
        }

        // Clean data and apply
        container.clearDamageContext();
        container.updateHealth(entity);

        // Death processing
        HealthSystem.synchronizeEntity(entity); // send status to client before death or further processing so that client knows which body part caused death
        if (container.shouldDie()) {
            entity.setHealth(0.0F); // cannot use LivingEntity#die as that causes problems with xp/drops
            return;
        }

        // Unconscious state processing
        if (BloodSystem.hasBloodDataIntegration(entity) && !entity.level().isClientSide()) {
            RandomSource random = entity.getRandom();
            BloodData bloodData = BloodSystem.getBloodData(entity);
            MedSystemConfig config = MedicalSystem.getConfig();
            int limbLostCount = lostBodyParts.size();
            if (!bloodData.isUnconscious() && config.allowUnconsciousOnLimbLost && limbLostCount > 0) {
                float unconsciousChance = limbLostCount * AttributeSystem.getFloatValue(entity, MedSystemAttributes.UNCONSCIOUS_ON_LIMB_LOSS_CHANCE, 0.2F);
                if (unconsciousChance > 0.0F && random.nextFloat() < unconsciousChance) {
                    int unconsciousDuration = limbLostCount * Duration.seconds(10).tickValue();
                    bloodData.setOrExtendedUnconsciousTime(unconsciousDuration, BloodData.UnconsciousInfo.PAIN);
                }
            }

            // TODO melee/projectile damage unconscious state after armor api rework

            bloodData.sync(entity);
        }

        // disable sprinting if entity can no longer sprint
        MovementStaminaComponent component = EnergySystem.MOVEMENT_STAMINA.getComponent();
        if (entity.isSprinting() && !component.canSprint(entity)) {
            entity.setSprinting(false);
        }
    }

    @SubscribeEvent
    private void onWeightUpdate(EntityWeightUpdateEvent event) {
        LivingEntity entity = event.getEntity();
        float factor = event.getOverweightFactor();
        if (!HealthSystem.hasCustomHealth(entity))
            return;
        HealthContainer container = HealthSystem.getHealthData(entity);
        StatusEffectMap effects = container.getGlobalStatusEffects();
        if (factor > 0.0F) {
            effects.replace(new OverweightStatusEffect(factor >= 1.0F));
        } else {
            effects.remove(StatusEffectSubmitter.NOOP, MedSystemStatusEffects.OVERWEIGHT, container, entity, null);
        }
        HealthSystem.synchronizeEntity(entity);
    }

    @SubscribeEvent
    private void canSprint(StaminaEvent.CanSprint event) {
        LivingEntity entity = event.getEntity();
        MedSystemConfig config = MedicalSystem.getConfig();
        if (config.statusEffects.enableStatusEffects && HealthSystem.isMovementRestricted(entity) && !HealthSystem.hasPainRelief(entity)) {
            event.setCanSprint(false);
        }
    }

    @SubscribeEvent
    private void onSprinted(StaminaEvent.AfterSprint event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        MedSystemConfig config = MedicalSystem.getConfig();
        long gameTime = level.getGameTime();
        if (level.isClientSide())
            return;
        if (config.statusEffects.enableStatusEffects && gameTime % 20L == 0L && HealthSystem.isMovementRestricted(entity)) {
            RegistryAccess access = entity.registryAccess();
            entity.hurtServer((ServerLevel) level, MedSystemDamageTypes.causeFractureDamage(access), 0.25F);
        }
    }

    @SubscribeEvent
    private void afterJump(StaminaEvent.AfterJump event) {
        LivingEntity entity = event.getEntity();
        MedSystemConfig config = MedicalSystem.getConfig();
        Level level = entity.level();
        if (level.isClientSide())
            return;
        if (config.statusEffects.enableStatusEffects && HealthSystem.isMovementRestricted(entity)) {
            RegistryAccess access = entity.registryAccess();
            entity.hurtServer((ServerLevel) level, MedSystemDamageTypes.causeFractureDamage(access), 0.50F);
        }
    }

    @SubscribeEvent
    private void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled())
            return;
        LivingEntity entity = event.getEntity();
        MedSystemConfig config = MedicalSystem.getConfig();
        DamageSource source = event.getSource();
        if (!event.isCanceled() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && entity instanceof Player player) {
            BloodData bloodData = BloodSystem.getBloodData(player);
            BloodStatus status = BloodStatus.fromBloodLevelPercentage(bloodData.getBloodVolumePercentage());
            BloodData.UnconsciousInfo unconsciousInfo = bloodData.getUnconsciousInfo();
            if (status == BloodStatus.DEATH || (unconsciousInfo != null && unconsciousInfo.causesDeath()))
                return; // no rescue on full blood loss, small workaround for immediate death
            RandomSource random = player.getRandom();
            if (random.nextFloat() < config.unconsciousOnDeathChance) {
                HealthContainer container = HealthSystem.getHealthData(player);
                if (!config.allowUnconsciousOnHeadDeath) {
                    boolean allPartsDead = container.getBodyPartStream()
                            .filter(part -> part.getGroup() == BodyPartGroup.HEAD)
                            .allMatch(BodyPart::isDead);
                    // no head body part alive, terminate further processing logic
                    if (allPartsDead)
                        return;
                }
                event.setCanceled(true);
                player.invulnerableTime = Math.max(player.invulnerableTime, config.rescueInvulnerabilityGracePeriod * 20);
                // recover vital body part health - otherwise player would immediately "die" again
                container.getBodyPartStream().forEach(part -> {
                    if (part.isDead() && part.isVital()) {
                        part.heal(1.0F);
                    }
                });
                container.updateHealth(player);
                HealthSystem.synchronizeEntity(player);

                // set unconscious
                bloodData.setUnconsciousTime(
                        Duration.seconds(config.rescueWaitDuration).tickValue(),
                        BloodData.UnconsciousInfo.DEATH
                );
                bloodData.sync(player);
            }
        }

        if (!event.isCanceled() && HealthSystem.hasCustomHealth(entity)) {
            HealthContainer container = HealthSystem.getHealthData(entity);
            container.invalidate();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    private void addItemstackTooltips(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item.TooltipContext context = event.getContext();
        List<Component> tooltip = event.getToolTip();
        TooltipFlag flag = event.getFlags();
        Consumer<Component> adder = tooltip::add;

        stack.addToTooltip(MedSystemItemComponents.HEAL_ATTRIBUTES, context, adder, flag);
        stack.addToTooltip(MedSystemItemComponents.SIDE_EFFECTS, context, adder, flag);
    }

    @SubscribeEvent
    private void onItemUseFinished(LivingEntityUseItemEvent.Finish event) {
        ItemStack stack = event.getItem();
        LivingEntity entity = event.getEntity();
        if (stack.has(MedSystemItemComponents.SIDE_EFFECTS)) {
            SideEffectHolder holder = stack.get(MedSystemItemComponents.SIDE_EFFECTS);
            InteractionTarget target = stack.get(MedSystemItemComponents.INTERACTION_TARGET);
            String targetLimb = target != null ? target.limbCode() : null;
            LivingEntity targetEntity = entity;
            if (target != null) {
                targetEntity = target.getTargetLivingEntity(entity);
            }
            if (!HealthSystem.hasCustomHealth(targetEntity))
                return;
            HealthContainer container = HealthSystem.getHealthData(targetEntity);
            BodyPart part = container.getBodyPart(targetLimb);
            holder.apply(targetEntity, container, part);
        }
    }

    @SubscribeEvent
    private void adjustHitboxSize(EntityEvent.Size event) {
        Entity entity = event.getEntity();
        if (entity.getType() == EntityType.PLAYER && event.getPose() == BloodData.UNCONSCIOUS_POSE) {
            Player player = (Player) entity;
            if (BloodSystem.isEntityUnconscious(player)) {
                event.setNewSize(BloodData.PLAYER_UNCONSCIOUS_DIMENSIONS);
            }
        }
    }

    @SubscribeEvent
    private void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        BloodData data = BloodSystem.getBloodData(player);
        data.updateEffects(player);
    }
}
