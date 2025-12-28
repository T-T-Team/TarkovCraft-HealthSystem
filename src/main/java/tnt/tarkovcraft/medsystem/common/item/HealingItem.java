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
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import tnt.tarkovcraft.core.common.skill.SkillSystem;
import tnt.tarkovcraft.core.util.helper.TextHelper;
import tnt.tarkovcraft.medsystem.api.heal.EffectRecovery;
import tnt.tarkovcraft.medsystem.api.heal.HealItemAttributes;
import tnt.tarkovcraft.medsystem.api.heal.HealthRecovery;
import tnt.tarkovcraft.medsystem.api.heal.Surgery;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.*;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.common.init.MedSystemSkillEvents;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;
import tnt.tarkovcraft.medsystem.network.message.S2C_OpenBodyPartSelectScreen;

import java.util.*;
import java.util.stream.Collectors;

public class HealingItem extends InteractableItem {

    private UseAnim selfUseAnimation = UseAnim.BOW; // when healing self
    private UseAnim otherUseAnimation = UseAnim.BOW; // when healing others

    public HealingItem(Properties properties) {
        super(properties);
    }

    public HealingItem withUseAnimations(UseAnim selfUseAnimation, UseAnim otherUseAnimation) {
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
        return attributes.canUseOn(target, origin, itemStack, HealthSystem.getHealthData(target));
    }

    @Override
    protected boolean tryInitiateExistingInteraction(ItemStack itemStack, InteractionTarget interaction, LivingEntity target, Player origin) {
        HealthContainer container = HealthSystem.getHealthData(target);
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
                this.selectBodyPart(interaction, itemStack, target);
            }
            if (interaction.isLimbSelected()) {
                setActiveInteraction(itemStack, interaction.toImmutable());
                return InteractionResult.SUCCESS;
            } else if (!level.isClientSide()) {
                PacketDistributor.sendToPlayer((ServerPlayer) origin, new S2C_OpenBodyPartSelectScreen(interaction));
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
                HealthContainer container = HealthSystem.getHealthData(target);
                InteractionTarget activeInteraction = this.getActiveInteraction(itemStack);
                Limb part = activeInteraction != null && TextHelper.isNotBlank(activeInteraction.limbCode()) && container.hasLimb(activeInteraction.limbCode())
                        ? container.getLimbByCode(activeInteraction.limbCode())
                        : null;

                if (level instanceof ServerLevel serverLevel) {
                    SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.HEALING_USED, origin);
                    if (itemStack.isDamageableItem()) {
                        itemStack.hurtAndBreak(1, serverLevel, origin, item -> origin.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
                    } else {
                        itemStack.consume(1, origin);
                    }
                }
                float leftover = container.heal(amount, part);
                if (leftover == amount) {
                    origin.useItemRemaining = 0;
                }
                if (leftover > 0 && container.canHeal()) {
                    container.heal(amount, null);
                }
                // rescue logic
                if (!interaction.self() && target instanceof Player player) {
                    BloodData bloodData = BloodSystem.getBloodData(player);
                    BloodData.UnconsciousInfo info = bloodData.getUnconsciousInfo();
                    if (bloodData.isUnconscious() && info.causesDeath()) {
                        bloodData.setUnconsciousTime(BloodSystem.RESCUE_WAKE_UP_DELAY, BloodData.UnconsciousInfo.PAIN);
                        bloodData.sync(player);
                    }
                }
                container.updateHealth(target);
                if (cycleIndex + 1 > cycleLimit || (part != null && !attributes.canUseOnPart(part, itemStack, container, interaction.self(), target))) {
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

        HealthContainer container = HealthSystem.getHealthData(target);
        Limb part = container.hasLimb(targetLimb) ? container.getLimbByCode(targetLimb) : null;
        int consume = 0;
        // dead limb recovery
        if (attributes.isSurgeryItem()) {
            Surgery surgery = attributes.surgery();
            consume++; // dead limb fix has hardcoded consumption value of 1
            if (part.isDead()) {
                SkillSystem.trigger(MedSystemSkillEvents.LIMB_FIXED, origin); // reward the healer
                part.setHealth(surgery.healthAfterHeal());
                surgery.addRecoveryAttributes(target, part);
            }
        }
        // effect recovery + consumption for recovery
        List<EffectRecovery> recoveries = attributes.recoveries();
        for (EffectRecovery recovery : recoveries) {
            if (recovery.canRecover(container, part) && checkDurability(itemStack, consume + recovery.consumption())) {
                recovery.recover(target, container, part);
                consume += recovery.consumption();
            }
        }
        // Apply durability reduction
        Level level = origin.level();
        if (!level.isClientSide()) {
            int consumeAmount = Math.max(1, consume);
            SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.HEALING_USED, origin, consumeAmount);
            if (itemStack.isDamageableItem()) {
                itemStack.hurtAndBreak(consumeAmount, (ServerLevel) level, origin, item -> origin.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
            } else {
                itemStack.consume(1, origin);
            }
        }
        container.updateHealth(target);
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
        return attributes.getUseDuration(72000);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
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
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(SimpleHealingItem.getCommonDurabilityLabel(stack));
    }

    public static boolean checkDurability(ItemStack stack, int durabilityUse) {
        int maxDamage = Math.max(stack.getMaxDamage(), 1) - stack.getDamageValue();
        return durabilityUse <= maxDamage;
    }

    private HealItemAttributes getHealingAttributes(ItemStack itemStack) {
        return itemStack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
    }

    private void selectBodyPart(InteractionTarget.Mutable activeTarget, ItemStack itemStack, LivingEntity entity) {
        HealthContainer container = HealthSystem.getHealthData(entity);
        HealItemAttributes attributes = this.getHealingAttributes(itemStack);
        List<BodyPartWithPriority> bodyParts = container.getLimbsAsStream()
                .map(part -> new BodyPartWithPriority(part, part.isVital() ? WoundPriorities.VITAL_PART_MULTIPLIER : 1.0F))
                .toList();

        if (attributes.isSurgeryItem()) {
            bodyParts.forEach(this::addSurgeryHealingPriorities);
        }
        if (attributes.isRecoveryItem()) {
            bodyParts.forEach(part -> this.addStatusEffectHealingPriorities(part, attributes.recoveries(), container));
        }
        if (attributes.isHealing()) {
            bodyParts.forEach(this::addHealthHealingPriorities);
        }

        bodyParts.stream()
                .filter(BodyPartWithPriority::isViable)
                .max(Comparator.comparingInt(BodyPartWithPriority::priority))
                .ifPresent(priorityPart -> activeTarget.setLimbCode(priorityPart.limb.getLimbCode()));
    }

    private void addSurgeryHealingPriorities(BodyPartWithPriority part) {
        if (part.limb.isDead()) {
            LimbType group = part.limb.getType();
            part.add(WoundPriorities.SURGERY_BASE + group.getSurgeryHealingPriority());
        }
    }

    private void addStatusEffectHealingPriorities(BodyPartWithPriority part, List<EffectRecovery> recoveries, HealthContainer container) {
        Limb limb = part.limb;
        Collection<StatusEffect> statusEffects = new ArrayList<>(limb.getStatusEffects().listEffects());
        if (limb == container.getRootLimb()) {
            statusEffects.addAll(container.getGlobalStatusEffects().listEffects());
        }
        Set<StatusEffectType<?>> uniqueTypes = statusEffects.stream()
                .map(StatusEffect::getType)
                .collect(Collectors.toSet());
        if (!uniqueTypes.isEmpty()) {
            for (EffectRecovery recovery : recoveries) {
                StatusEffectType<?> type = recovery.effect().value();
                if (uniqueTypes.contains(type)) {
                    part.add(type.getHealingPriority());
                }
            }
        }
    }

    private void addHealthHealingPriorities(BodyPartWithPriority part) {
        Limb limb = part.limb;
        float missingAmount = limb.getMaxHealAmount();
        if (!limb.isDead() && missingAmount > 0) {
            part.add(Mth.ceil(WoundPriorities.HEALTH_UNIT * missingAmount));
        }
    }

    private static final class BodyPartWithPriority {

        private final Limb limb;
        private final float multiplier;
        private int priority;

        public BodyPartWithPriority(Limb limb, float multiplier) {
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
