package tnt.tarkovcraft.medsystem.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.core.common.skill.SkillSystem;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemSkillEvents;

import java.util.Objects;
import java.util.function.Consumer;

public class SimpleHealingItem extends InteractableItem {

    private ItemUseAnimation selfUseAnimation = ItemUseAnimation.BOW; // when healing self
    private ItemUseAnimation otherUseAnimation = ItemUseAnimation.BOW; // when healing others

    public SimpleHealingItem(Properties properties) {
        super(properties);
    }

    public SimpleHealingItem withUseAnimations(ItemUseAnimation selfUseAnimation, ItemUseAnimation otherUseAnimation) {
        this.selfUseAnimation = Objects.requireNonNull(selfUseAnimation);
        this.otherUseAnimation = Objects.requireNonNull(otherUseAnimation);
        return this;
    }

    @Override
    protected boolean canUseItem(ItemStack itemStack, LivingEntity target, LivingEntity origin) {
        return super.canUseItem(itemStack, target, origin) && HealthSystem.hasCustomHealth(target);
    }

    @Override
    protected boolean tryInitiateExistingInteraction(ItemStack itemStack, InteractionTarget interaction, LivingEntity target, Player origin) {
        return false;
    }

    @Override
    protected InteractionResult initiateInteraction(ItemStack itemStack, InteractionTarget.Mutable interaction, LivingEntity target, Player origin) {
        return InteractionResult.SUCCESS;
    }

    @Override
    protected boolean updateInteraction(Level level, ItemStack itemStack, InteractionTarget interaction, LivingEntity target, LivingEntity origin, int remainingUseTicks) {
        return true;
    }

    @Override
    protected ItemStack finishInteraction(ItemStack itemStack, InteractionTarget interaction, LivingEntity target, LivingEntity origin) {
        Level level = origin.level();
        if (!level.isClientSide()) {
            SkillSystem.triggerAndSynchronize(MedSystemSkillEvents.HEALING_USED, origin, 1);
            if (itemStack.isDamageableItem()) {
                itemStack.hurtAndBreak(1, (ServerLevel) level, origin, item -> origin.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
            } else {
                itemStack.consume(1, origin);
            }
        }
        return itemStack;
    }

    @Override
    protected @Nullable Component getInteractionLabel(ItemStack itemStack, InteractionTarget interaction, LivingEntity target, LivingEntity origin, int time, boolean infinite) {
        return getCommonInteractionLabel(interaction, target, time, infinite);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 60;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        InteractionTarget interaction = this.getActiveInteraction(stack);
        return interaction != null && !interaction.self() ? this.otherUseAnimation : this.selfUseAnimation;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(getCommonDurabilityLabel(stack));
    }

    public static Component getCommonDurabilityLabel(ItemStack itemStack) {
        int max = Math.max(itemStack.getMaxDamage(), 1);
        int damage = max - itemStack.getDamageValue();
        Component durability = Component.literal(damage + "/" + max).withStyle(ChatFormatting.RED);
        return Component.translatable("tooltip.medsystem.item.durability", durability).withStyle(ChatFormatting.GRAY);
    }

    public static Component getCommonInteractionLabel(InteractionTarget interaction, LivingEntity target, int time, boolean infinite) {
        if (interaction.self()) {
            return infinite
                    ? Component.translatable("label.medsystem.healing.self.infinite")
                    : Component.translatable("label.medsystem.healing.self", formatUsageDuration(time));
        } else {
            return infinite
                    ? Component.translatable("label.medsystem.healing.other.infinite", target.getDisplayName())
                    : Component.translatable("label.medsystem.healing.other", target.getDisplayName(), formatUsageDuration(time));
        }
    }
}
