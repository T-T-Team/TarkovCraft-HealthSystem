package tnt.tarkovcraft.medsystem.common;

import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.damagesource.IReductionFunction;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.ArmorStat;
import tnt.tarkovcraft.medsystem.common.health.*;
import tnt.tarkovcraft.medsystem.common.health.math.DamageDistributor;
import tnt.tarkovcraft.medsystem.common.health.math.HitCalculator;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;

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
            float leftover = entity.getData(MedSystemDataAttachments.HEALTH_CONTAINER).heal(amount, null);
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
        if (!entity.hasData(MedSystemDataAttachments.HEALTH_CONTAINER))
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
                .map(BodyPartGroup::getArmorSlot)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        context.setAffectedSlots(new ArrayList<>());
        float armor = this.calculateArmor(entity, context, protectedSlots);
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
            container.hurt(entry.getValue(), entry.getKey());
        }
        container.clearDamageContext();
        container.updateHealth(entity);
        if (container.shouldDie()) {
            entity.die(event.getSource());
        } else {
            HealthSystem.synchronizeEntity(entity);
        }
    }

    private float calculateArmor(LivingEntity entity, DamageContext context, Collection<EquipmentSlot> protectedSlots) {
        double result = 0.0D;
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
