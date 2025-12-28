package tnt.tarkovcraft.medsystem.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.common.status.BloodContainer;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodStatus;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

import java.util.Locale;
import java.util.function.Consumer;

public class BloodBagItem extends InteractableItem {

    public BloodBagItem(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canUseItem(ItemStack itemStack, LivingEntity target, LivingEntity origin) {
        if (!super.canUseItem(itemStack, target, origin)) {
            return false;
        }
        // BloodContainer must be defined
        BloodContainer container = this.getContainer(itemStack);
        if (container == null) {
            return false;
        }
        // entity needs to have blood data integration
        if (!BloodSystem.hasBloodDataIntegration(target)) {
            return false;
        }
        BloodData bloodData = BloodSystem.getBloodData(target);
        boolean refillMode = this.isRefillMode(container, bloodData);
        return refillMode || !bloodData.hasFullBloodVolume();
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
        BloodContainer container = this.getContainer(itemStack);
        BloodData bloodData = BloodSystem.getBloodData(target);
        boolean refillMode = this.isRefillMode(container, bloodData);
        BloodContainer updated;
        if (refillMode) {
            float fillAmount = Math.min(container.getMissingCapacity(), bloodData.getBloodVolume());
            float extracted = bloodData.extract(fillAmount);
            updated = container.fill(extracted);
        } else {
            float extractionAmount = Math.min(container.value(), bloodData.getMissingBloodVolume());
            float filled = bloodData.insert(extractionAmount);
            updated = container.extract(filled);
        }
        if (updated.isEmpty() && !updated.refillable()) {
            itemStack.consume(1, target);
        } else {
            itemStack.set(MedSystemItemComponents.BLOOD_CONTAINER, updated);
        }
        return itemStack;
    }

    @Override
    protected @Nullable Component getInteractionLabel(ItemStack itemStack, InteractionTarget interaction, LivingEntity target, LivingEntity origin, int time, boolean infinite) {
        BloodContainer container = this.getContainer(itemStack);
        BloodData bloodData = BloodSystem.getBloodData(target);
        boolean refillMode = this.isRefillMode(container, bloodData);
        String key = refillMode
                ? "label.medsystem.blood_extraction"
                : "label.medsystem.blood_insertion";
        if (interaction.self()) {
            return Component.translatable(key + ".self", formatUsageDuration(time));
        } else {
            return Component.translatable(key + ".other", target.getDisplayName(), formatUsageDuration(time));
        }
    }

    @Override
    protected boolean canInteractWithEntity(ItemStack stack, LivingEntity entity, LivingEntity origin) {
        return BloodSystem.hasBloodDataIntegration(entity);
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        BloodContainer container = this.getContainer(stack);
        if (container == null) {
            return;
        }
        Component value = Component.translatable("label.medsystem.unit.liter", String.format(Locale.ROOT, "%.2f", container.value())).withStyle(ChatFormatting.RED);
        Component capacity = Component.translatable("label.medsystem.unit.liter", String.format(Locale.ROOT, "%.2f", container.capacity())).withStyle(ChatFormatting.RED);
        tooltipAdder.accept(Component.translatable("label.medsystem.blood", value, capacity).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 200;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    private BloodContainer getContainer(ItemStack itemStack) {
        return itemStack.get(MedSystemItemComponents.BLOOD_CONTAINER);
    }

    private boolean isRefillMode(BloodContainer container, BloodData data) {
        BloodStatus status = BloodStatus.fromBloodLevelPercentage(data.getBloodVolumePercentage());
        if (!container.refillable())
            return false;
        return (container.isEmpty() && status.isSameOrAbove(BloodStatus.MODERATE_BLOOD_LOSS)) || (data.hasFullBloodVolume() && !container.isFull());
    }
}
