package tnt.tarkovcraft.medsystem.common.item;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.common.skill.SkillSystem;
import tnt.tarkovcraft.core.util.helper.EntityHelper;
import tnt.tarkovcraft.core.util.helper.TextHelper;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.api.heal.EffectRecovery;
import tnt.tarkovcraft.medsystem.api.heal.HealItemAttributes;
import tnt.tarkovcraft.medsystem.api.heal.HealthRecovery;
import tnt.tarkovcraft.medsystem.api.heal.Surgery;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.*;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.common.init.MedSystemSkillEvents;
import tnt.tarkovcraft.medsystem.network.message.S2C_OpenLimbSelectScreen;
import tnt.tarkovcraft.medsystem.util.HealthHelper;

import java.util.*;
import java.util.function.Consumer;

public class HealingItem extends InteractableItem {

    private ItemUseAnimation selfUseAnimation = ItemUseAnimation.BOW; // when healing self
    private ItemUseAnimation otherUseAnimation = ItemUseAnimation.BOW; // when healing others

    public HealingItem(Properties properties) {
        super(properties);
    }

    public HealingItem withUseAnimations(ItemUseAnimation selfUseAnimation, ItemUseAnimation otherUseAnimation) {
        this.selfUseAnimation = Objects.requireNonNull(selfUseAnimation);
        this.otherUseAnimation = Objects.requireNonNull(otherUseAnimation);
        return this;
    }

    @Override
    protected boolean canUseItem(ItemStack itemStack, LivingEntity target, LivingEntity origin) {
        if (!super.canUseItem(itemStack, target, origin)) {
            return false;
        }
        if (!HealthSystem.hasCustomHealth(target)) {
            return false;
        }
        HealItemAttributes attributes = this.getHealingAttributes(itemStack);
        if (attributes == null) {
            return false;
        }
        return attributes.canUseOn(target, origin, itemStack, HealthContainer.getAttached(target));
    }

    @Override
    protected boolean tryInitiateExistingInteraction(ItemStack itemStack, InteractionTarget interaction, LivingEntity target, Player origin) {
        HealthContainer container = HealthContainer.getAttached(target);
        HealItemAttributes attributes = this.getHealingAttributes(itemStack);
        if (attributes == null) {
            return false;
        }
        return !origin.isCrouching() && (attributes.applyGlobally() || container.hasLimb(interaction.limbCode()));
    }

    @Override
    protected InteractionResult initiateInteraction(ItemStack itemStack, InteractionTarget.Mutable interaction, LivingEntity target, Player origin) {
        HealItemAttributes attributes = this.getHealingAttributes(itemStack);
        if (!attributes.applyGlobally()) {
            Level level = origin.level();
            if (interaction.isSelf() && !origin.isCrouching()) {
                this.selectLimb(interaction, itemStack, target);
            }
            if (interaction.isLimbSelected()) {
                setActiveInteraction(itemStack, interaction.toImmutable());
                return InteractionResult.SUCCESS;
            } else if (!level.isClientSide()) {
                PacketDistributor.sendToPlayer((ServerPlayer) origin, new S2C_OpenLimbSelectScreen(interaction));
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean updateInteraction(Level level, ItemStack itemStack, InteractionTarget interaction, LivingEntity target, LivingEntity origin, int remainingUseTicks) {
        HealItemAttributes attributes = this.getHealingAttributes(itemStack);
        if (attributes == null) {
            return false;
        }
        HealthRecovery healthRecovery = attributes.health();
        if (healthRecovery == null)
            return true;

        int useDuration = this.getUseDuration(itemStack, origin);
        int usageTimeElapsed = useDuration - remainingUseTicks + 1;
        if (usageTimeElapsed % healthRecovery.cycleDuration() == 0) {
            int cycleLimit = healthRecovery.maxCycles() == 0 ? Integer.MAX_VALUE : healthRecovery.cycleDuration();
            int cycleIndex = usageTimeElapsed / healthRecovery.cycleDuration();
            if (cycleIndex < cycleLimit) {
                float amount = healthRecovery.healthPerCycle();
                HealthContainer container = HealthContainer.getAttached(target);
                InteractionTarget activeInteraction = this.getActiveInteraction(itemStack);
                Limb limb = activeInteraction != null && TextHelper.isNotBlank(activeInteraction.limbCode()) && container.hasLimb(activeInteraction.limbCode())
                        ? container.getLimbByCode(activeInteraction.limbCode())
                        : null;

                SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.HEALING_USED, origin);

                EntityHelper.hurtOrConsumeEquipmentItem(origin, itemStack, 1, EquipmentSlot.MAINHAND);

                // add health and cancel using item if fully healed
                LimbContainer limbContainer = container.getLimbContainer();
                float leftover = limbContainer.heal(amount, limb);
                if (leftover == amount) {
                    origin.useItemRemaining = 0;
                }
                if (leftover > 0 && HealthHelper.canHeal(container)) {
                    limbContainer.heal(amount, null);
                }

                // adjust vanilla health pool
                HealthHelper.synchronizeHealth(target, container);
                if (cycleIndex + 1 > cycleLimit || (limb != null && !attributes.canUseOnLimb(limb, itemStack, container, target))) {
                    origin.useItemRemaining = 0;
                } else {
                    HealthSystem.synchronizeEntity(target);
                }
            }
        }

        return !itemStack.isEmpty();
    }

    @Override
    protected ItemStack finishInteraction(ItemStack itemStack, InteractionTarget interaction, LivingEntity target, LivingEntity origin) {
        HealItemAttributes attributes = this.getHealingAttributes(itemStack);
        String targetLimb = interaction.limbCode();
        if (!attributes.applyGlobally() && TextHelper.isBlank(interaction.limbCode())) {
            return itemStack;
        }

        HealthContainer container = HealthContainer.getAttached(target);
        Limb limb = container.hasLimb(targetLimb) ? container.getLimbByCode(targetLimb) : container.getRootLimb();
        int consume = 0;
        // dead limb recovery
        if (attributes.isSurgeryItem()) {
            Surgery surgery = attributes.surgery();
            consume++; // dead limb fix has hardcoded consumption value of 1
            if (limb.isDead()) {
                SkillSystem.trigger(MedSystemSkillEvents.LIMB_FIXED, origin); // reward the healer
                limb.setHealth(surgery.recoveryHealth());
                surgery.onSurgeryFinished(target, limb);
            }
        }
        // effect recovery + consumption for recovery
        List<EffectRecovery> recoveries = attributes.recoveries();
        for (EffectRecovery recovery : recoveries) {
            if (recovery.canRecover(container, target, limb) && checkDurability(itemStack, consume + recovery.consumption())) {
                recovery.recover(container, target, limb);
                consume += recovery.consumption();
            }
        }
        // Consume effect application
        Level level = origin.level();
        List<ConsumeEffect> consumeEffects = attributes.effects();
        for (ConsumeEffect effect : consumeEffects) {
            effect.apply(level, itemStack, target);
        }
        // Apply durability reduction
        if (!level.isClientSide()) {
            int consumeAmount = Math.max(1, consume);
            SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.HEALING_USED, origin, consumeAmount);
            if (itemStack.isDamageableItem()) {
                itemStack.hurtAndBreak(consumeAmount, (ServerLevel) level, origin, item -> origin.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
            } else {
                itemStack.consume(1, origin);
            }
        }
        HealthHelper.synchronizeHealth(target, container);
        HealthSystem.synchronizeEntity(target);
        return itemStack;
    }

    @Override
    protected boolean shouldClearInteractionDataOnCancellation(ItemStack itemStack, LivingEntity entity, int count) {
        InteractionTarget interaction = this.getActiveInteraction(itemStack);
        return interaction != null && !interaction.self();
    }

    @Override
    protected @Nullable Component getInteractionLabel(ItemStack itemStack, InteractionTarget interaction, LivingEntity target, LivingEntity origin, int time, boolean infinite) {
        return SimpleHealingItem.getCommonInteractionLabel(interaction, target, time, infinite);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        HealItemAttributes attributes = stack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
        return attributes.getUseDuration(APPROXIMATELY_INFINITE_USE_DURATION);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        InteractionTarget interaction = this.getActiveInteraction(stack);
        return interaction != null && !interaction.self() ? this.otherUseAnimation : this.selfUseAnimation;
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return false;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFF0000;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(SimpleHealingItem.getCommonDurabilityLabel(stack));
    }

    public static boolean checkDurability(ItemStack stack, int durabilityUse) {
        int maxDamage = Math.max(stack.getMaxDamage(), 1) - stack.getDamageValue();
        return durabilityUse <= maxDamage;
    }

    private HealItemAttributes getHealingAttributes(ItemStack itemStack) {
        return itemStack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
    }

    private void selectLimb(InteractionTarget.Mutable activeTarget, ItemStack itemStack, LivingEntity entity) {
        HealthContainer container = HealthContainer.getAttached(entity);
        HealItemAttributes attributes = this.getHealingAttributes(itemStack);
        List<LimbWithPriority> limbs = container.getLimbContainer().getLimbs()
                .map(limb -> new LimbWithPriority(limb, limb.isVital() ? MedSystemConstants.HEAL_VITAL_PART_MULTIPLIER : 1.0F))
                .toList();

        if (attributes.isSurgeryItem()) {
            limbs.forEach(this::addSurgeryHealingPriorities);
        }
        if (attributes.isRecoveryItem()) {
            limbs.forEach(limb -> this.addStatusEffectHealingPriorities(container, entity, limb, attributes.recoveries()));
        }
        if (attributes.isHealing()) {
            limbs.forEach(this::addHealthHealingPriorities);
        }

        limbs.stream()
                .filter(LimbWithPriority::isViable)
                .max(Comparator.comparingInt(LimbWithPriority::priority))
                .ifPresent(limbWithPriority -> activeTarget.setLimbCode(limbWithPriority.limb.getLimbCode()));
    }

    private void addSurgeryHealingPriorities(LimbWithPriority part) {
        if (part.limb.isDead()) {
            LimbType group = part.limb.getType();
            part.add(MedSystemConstants.HEAL_SURGERY_BASE + group.getSurgeryHealingPriority());
        }
    }

    private void addStatusEffectHealingPriorities(HealthContainer container, LivingEntity entity, LimbWithPriority priorityLimb, List<EffectRecovery> recoveries) {
        Limb limb = priorityLimb.limb;
        StatusEffectMap statusEffects = limb.getStatusEffects();
        if (statusEffects.isEmpty())
            return;

        for (EffectRecovery recovery : recoveries) {
            Optional<StatusEffect> effect = recovery.applicator().findRecoverableEffect(container, entity, limb);
            int priority = effect.map(statusEffect -> statusEffect.getType().getHealingPriority(statusEffect))
                    .orElse(0);
            if (priority > 0) {
                priorityLimb.add(priority);
            }
        }
    }

    private void addHealthHealingPriorities(LimbWithPriority part) {
        Limb limb = part.limb;
        float missingAmount = limb.getMaxHealAmount();
        if (!limb.isDead() && missingAmount > 0) {
            part.add(Mth.ceil(MedSystemConstants.HEAL_HEALTH_UNIT * missingAmount));
        }
    }

    private static final class LimbWithPriority {

        private final Limb limb;
        private final float multiplier;
        private int priority;

        public LimbWithPriority(Limb limb, float multiplier) {
            this.limb = limb;
            this.multiplier = multiplier;
        }

        private void add(int amount) {
            this.priority += Mth.ceil(amount * this.multiplier);
        }

        private boolean isViable() {
            return this.priority > 0;
        }

        private int priority() {
            return this.priority;
        }

        @Override
        public String toString() {
            return limb.getLimbCode() + ": " + priority;
        }
    }
}
