package tnt.tarkovcraft.medsystem.common;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import tnt.tarkovcraft.core.api.MovementStaminaComponent;
import tnt.tarkovcraft.core.api.event.StaminaEvent;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.energy.EnergySystem;
import tnt.tarkovcraft.core.common.skill.SkillSystem;
import tnt.tarkovcraft.core.common.statistic.StatisticTracker;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.ArmorComponent;
import tnt.tarkovcraft.medsystem.common.health.*;
import tnt.tarkovcraft.medsystem.common.health.math.DamageDistributor;
import tnt.tarkovcraft.medsystem.common.health.math.HitCalculator;
import tnt.tarkovcraft.medsystem.common.init.*;

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
        }
    }

    @SubscribeEvent
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
        HitCalculator hitCalculator = HealthSystem.getHitCalculator(livingEntity, source, container);
        List<HitResult> hits = hitCalculator.calculateHits(livingEntity, source, container);
        if (hits == null || hits.isEmpty()) {
            event.setInvulnerable(true);
        } else {
            DamageContext context = new DamageContext(livingEntity, source);
            context.setHits(hits);
            context.setHitCalculator(hitCalculator);
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
        DamageContext context = container.getDamageContext();
        DamageDistributor damageDistributor = context.getDamageDistributor(container);
        Map<BodyPart, Float> distributedDamage = damageDistributor.distribute(context, container, event.getNewDamage());
        float totalDamage = distributedDamage.values().stream().reduce(0.0F, Float::sum);
        List<BodyPart> lostBodyParts = new ArrayList<>();
        for (Map.Entry<BodyPart, Float> entry : distributedDamage.entrySet()) {
            container.hurt(entity, event.getSource(), entry.getValue(), entry.getKey(), lostBodyParts::add);
        }
        SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.DAMAGE_TAKEN, entity, totalDamage);
        container.clearDamageContext();
        container.updateHealth(entity);
        float deathChance = lostBodyParts.isEmpty() ? 0.0F : MedicalSystem.getConfig().limbLossDeathCauseChance;
        if (deathChance > 0.0F) {
            float deathChanceMultiplier = AttributeSystem.getFloatValue(entity, MedSystemAttributes.LIMB_DEATH_CHANCE, 1.0F);
            deathChance *= (deathChanceMultiplier / lostBodyParts.size());
        }
        HealthSystem.synchronizeEntity(entity); // send status to client before death or further processing so that client knows which body part caused death
        if (container.shouldDie() || (deathChance > 0.0F && entity.getRandom().nextFloat() < deathChance)) {
            entity.setHealth(0.0F); // cannot use LivingEntity#die as that causes problems with xp/drops
        } else {
            // disable sprinting
            MovementStaminaComponent component = EnergySystem.MOVEMENT_STAMINA.getComponent();
            if (entity.isSprinting() && component.isAttached(entity)) {
                if (!component.canSprint(entity)) {
                    entity.setSprinting(false);
                }
            }
        }
    }

    @SubscribeEvent
    private void canSprint(StaminaEvent.CanSprint event) {
        LivingEntity entity = event.getEntity();
        if (HealthSystem.isMovementRestricted(entity) && !HealthSystem.hasPainRelief(entity)) {
            event.setCanSprint(false);
        }
    }

    @SubscribeEvent
    private void onSprinted(StaminaEvent.AfterSprint event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.level();
        long gametime = level.getGameTime();
        if (gametime % 20L == 0L && HealthSystem.isMovementRestricted(entity)) {
            RegistryAccess access = entity.registryAccess();
            DamageSource source = new DamageSource(MedSystemDamageTypes.of(access, MedSystemDamageTypes.BROKEN_LEG));
            entity.hurt(source, 1.0F);
        }
    }

    @SubscribeEvent
    private void afterJump(StaminaEvent.AfterJump event) {
        LivingEntity entity = event.getEntity();
        if (HealthSystem.isMovementRestricted(entity)) {
            RegistryAccess access = entity.registryAccess();
            DamageSource source = new DamageSource(MedSystemDamageTypes.of(access, MedSystemDamageTypes.BROKEN_LEG));
            entity.hurt(source, 2.5F);
        }
    }

    @SubscribeEvent
    private void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled())
            return;
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        Entity killer = source.getEntity();
        if (killer != null && HealthSystem.hasCustomHealth(entity)) {
            HealthContainer container = entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
            boolean headshot = container.getBodyPartStream().anyMatch(part -> part.getGroup() == BodyPartGroup.HEAD && part.isDead());
            if (headshot) {
                StatisticTracker.incrementOptional(killer, MedSystemStats.HEADSHOTS);
                if (entity.getType() == EntityType.PLAYER) {
                    StatisticTracker.increment(killer, MedSystemStats.PLAYER_HEADSHOTS);
                }
            }
        }
        if (HealthSystem.hasCustomHealth(entity)) {
            HealthContainer container = HealthSystem.getHealthData(entity);
            container.clearBoundData(entity);
        }
    }

    @SubscribeEvent
    private void addItemstackTooltips(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item.TooltipContext context = event.getContext();
        List<Component> tooltip = event.getToolTip();
        TooltipFlag flag = event.getFlags();
        Consumer<Component> adder = tooltip::add;

        stack.addToTooltip(MedSystemItemComponents.HEAL_ATTRIBUTES, context, adder, flag);
    }

    @SubscribeEvent
    private void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        HealthSystem.getHealthData(player).tick(player);
    }
}
