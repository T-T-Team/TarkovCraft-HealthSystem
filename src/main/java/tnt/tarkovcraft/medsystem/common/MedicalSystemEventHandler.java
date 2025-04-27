package tnt.tarkovcraft.medsystem.common;

import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.damagesource.IReductionFunction;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import tnt.tarkovcraft.core.api.MovementStaminaComponent;
import tnt.tarkovcraft.core.api.event.StaminaEvent;
import tnt.tarkovcraft.core.common.energy.EnergySystem;
import tnt.tarkovcraft.core.common.statistic.StatisticTracker;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.ArmorStat;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.health.*;
import tnt.tarkovcraft.medsystem.common.health.math.DamageDistributor;
import tnt.tarkovcraft.medsystem.common.health.math.HitCalculator;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStats;

import java.util.*;
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
        if (amount > 0.0F && entity.hasData(MedSystemDataAttachments.HEALTH_CONTAINER)) {
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
        if (!entity.hasData(MedSystemDataAttachments.HEALTH_CONTAINER))
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
        MedSystemConfig config = MedicalSystem.getConfig();
        if (!entity.hasData(MedSystemDataAttachments.HEALTH_CONTAINER) || config.simpleArmorCalculation)
            return;
        HealthContainer container = entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        DamageContext context = container.getDamageContext();
        Set<EquipmentSlot> hitSlots = new HashSet<>(context.getAffectedSlots());
        Set<EquipmentSlot> armorSlots = new HashSet<>(event.getArmorMap().keySet());
        for (EquipmentSlot slot : armorSlots) {
            if (!hitSlots.contains(slot)) {
                event.getArmorMap().remove(slot);
            }
        }
    }

    // Entity armor damage recalculation
    @SubscribeEvent
    private void onLivingDamage(LivingIncomingDamageEvent event) {
        // calculate correct damage for armor and so on
        LivingEntity entity = event.getEntity();
        if (!entity.hasData(MedSystemDataAttachments.HEALTH_CONTAINER))
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
        // Protected hitbox groups
        EnumSet<BodyPartGroup> protectedGroups = EnumSet.noneOf(BodyPartGroup.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor())
                continue;
            ItemStack itemStack = entity.getItemBySlot(slot);
            if (itemStack.isEmpty())
                continue;

            protectedGroups.addAll(getItemProtectedGroups(itemStack, slot));
        }
        // remove not affected groups
        protectedGroups.removeIf(group -> !hitGroups.contains(group));
        // armor reduction calculation preparation

        Set<EquipmentSlot> protectedSlots = protectedGroups.stream()
                .flatMap(group -> group.getArmorSlots().stream())
                .collect(Collectors.toSet());

        context.setAffectedSlots(new ArrayList<>());
        MedSystemConfig config = MedicalSystem.getConfig();
        float armor = config.simpleArmorCalculation ? entity.getArmorValue() : this.calculateArmor(entity, context, protectedSlots);
        float armorToughness = (float) entity.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float previousDamage = event.getAmount();
        float damageAfterArmorAbsorb = CombatRules.getDamageAfterAbsorb(entity, previousDamage, event.getSource(), armor, armorToughness);
        float reduction = previousDamage - damageAfterArmorAbsorb;
        event.addReductionModifier(DamageContainer.Reduction.ARMOR, new SetReductionFunction(reduction));
    }

    // Entity damage application
    @SubscribeEvent
    private void onLivingApplyDamage(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasData(MedSystemDataAttachments.HEALTH_CONTAINER))
            return;
        HealthContainer container = entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        DamageContext context = container.getDamageContext();
        DamageDistributor damageDistributor = context.getDamageDistributor(container);
        Map<BodyPart, Float> distributedDamage = damageDistributor.distribute(context, container, event.getNewDamage());
        for (Map.Entry<BodyPart, Float> entry : distributedDamage.entrySet()) {
            container.hurt(entity, entry.getValue(), entry.getKey(), false);
        }
        container.updateEffects(entity);
        container.clearDamageContext();
        container.updateHealth(entity);
        if (container.shouldDie()) {
            entity.setHealth(0.0F);
        } else {
            // disable sprinting
            MovementStaminaComponent component = EnergySystem.MOVEMENT_STAMINA.getComponent();
            if (entity.isSprinting() && component.isAttached(entity)) {
                if (!component.canSprint(entity)) {
                    entity.setSprinting(false);
                }
            }
            HealthSystem.synchronizeEntity(entity);
        }
    }

    @SubscribeEvent
    private void canSprint(StaminaEvent.CanSprint event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasData(MedSystemDataAttachments.HEALTH_CONTAINER))
            return;
        // TODO ignore if under painkiller effect - or maybe work with painkiller strength
        // TODO check for parts with movement blocking status effects

        HealthContainer container = entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        if (container.getBodyPartStream().anyMatch(part -> part.getGroup() == BodyPartGroup.LEG && part.isDead())) {
            event.setCanSprint(false);
        }
    }

    @SubscribeEvent
    private void onSprinted(StaminaEvent.AfterSprint event) {
        // TODO if sprinting with painkiller effect on apply sprint damage
    }

    @SubscribeEvent
    private void afterJump(StaminaEvent.AfterJump event) {
        LivingEntity entity = event.getEntity();
        if (!entity.hasData(MedSystemDataAttachments.HEALTH_CONTAINER))
            return;

        HealthContainer container = entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
        // TODO check for broken legs too
        float damage = container.getBodyPartStream()
                .filter(part -> part.getGroup() == BodyPartGroup.LEG && part.isDead())
                .count();
        entity.hurt(entity.damageSources().fall(), damage);
    }

    @SubscribeEvent
    private void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled())
            return;
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        Entity killer = source.getEntity();
        if (killer != null && entity.hasData(MedSystemDataAttachments.HEALTH_CONTAINER)) {
            HealthContainer container = entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER);
            boolean headshot = container.getBodyPartStream().anyMatch(part -> part.getGroup() == BodyPartGroup.HEAD && part.isDead());
            if (headshot) {
                StatisticTracker.incrementOptional(killer, MedSystemStats.HEADSHOTS);
                if (entity instanceof Player player) {
                    StatisticTracker.increment(player, MedSystemStats.PLAYER_HEADSHOTS);
                }
            }
        }
    }

    private float calculateArmor(LivingEntity entity, DamageContext context, Collection<EquipmentSlot> protectedSlots) {
        double result = entity.getAttribute(Attributes.ARMOR).getBaseValue();
        for (EquipmentSlot slot : protectedSlots) {
            ItemStack itemStack = entity.getItemBySlot(slot);
            if (itemStack.isEmpty())
                continue;
            context.getAffectedSlots().add(slot);
            ItemAttributeModifiers modifiers = itemStack.getAttributeModifiers();
            for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
                if (entry.attribute().is(Attributes.ARMOR)) {
                    AttributeModifier modifier = entry.modifier();
                    if (modifier.operation() == AttributeModifier.Operation.ADD_VALUE) {
                        result += modifier.amount();
                    }
                }
            }
        }
        return (float) result;
    }

    public static Collection<BodyPartGroup> getItemProtectedGroups(ItemStack itemStack, EquipmentSlot slot) {
        if (itemStack.has(MedSystemItemComponents.ARMOR_STAT)) {
            ArmorStat stat = itemStack.get(MedSystemItemComponents.ARMOR_STAT);
            return stat.protectedArea();
        } else {
            return BodyPartGroup.getProtectedByEquipment(slot);
        }
    }

    private record SetReductionFunction(float amount) implements IReductionFunction {

        @Override
        public float modify(DamageContainer container, float reductionIn) {
            return this.amount();
        }
    }
}
