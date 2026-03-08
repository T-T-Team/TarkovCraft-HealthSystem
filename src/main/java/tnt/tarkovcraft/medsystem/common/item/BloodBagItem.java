package tnt.tarkovcraft.medsystem.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.common.item.LeftClickListener;
import tnt.tarkovcraft.core.network.message.S2C_MakeParticles;
import tnt.tarkovcraft.core.util.UserActionResult;
import tnt.tarkovcraft.core.util.helper.EntityHelper;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.client.particle.BloodDripParticleOptions;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodContainer;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodContainerMode;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystem;
import tnt.tarkovcraft.medsystem.common.blood_system.assignment.EntityBloodSystemDefinition;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;

import java.util.List;
import java.util.Collections;
import java.util.Locale;

public class BloodBagItem extends InteractableItem implements LeftClickListener {

    public BloodBagItem(Properties properties) {
        super(properties);
    }

    @Override
    public void onLeftClick(Player player, Level level, ItemStack itemStack, @Nullable BlockPos blockPos) {
        if (!level.isClientSide()) {
            BloodContainerMode mode = getActiveMode(itemStack);
            BloodContainer container = getBloodContainer(itemStack);
            if (container == null)
                return;
            BloodContainerMode nextMode = mode.next(container);
            itemStack.set(MedSystemItemComponents.BLOOD_CONTAINER_MODE, nextMode);
            Component message = Component.translatable("label.medsystem.blood_container.mode_changed", nextMode.getLabel());
            player.displayClientMessage(message, true);
        }
    }

    @Override
    protected boolean canUseItem(ItemStack itemStack, LivingEntity target, LivingEntity origin) {
        if (!super.canUseItem(itemStack, target, origin)) {
            return false;
        }
        // BloodContainer must be defined
        BloodContainer container = getBloodContainer(itemStack);
        if (container == null) {
            return false;
        }
        // entity needs to have blood data integration
        if (!BloodSystemManager.isEnabled(target)) {
            return false;
        }
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(target);
        BloodContainerMode mode = getActiveMode(itemStack);
        UserActionResult<Boolean> result = container.canUseMode(mode, bloodSystem);
        if (!result.isSuccess()) {
            Component reason = result.message();
            EntityHelper.displayClientMessage(origin, reason);
            return false;
        }
        return true;
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
        BloodContainerMode mode = getActiveMode(itemStack);
        if (mode.isDraining() && (remainingUseTicks + 1) % 5 == 0) {
            BloodContainer container = getBloodContainer(itemStack);
            float drainAmount = Math.min(this.getDrainAmount(itemStack), container.value());
            BloodContainer drained = container.extract(drainAmount);
            if (!level.isClientSide()) {
                container.bloodType().flatMap(id -> MedicalSystem.BLOOD_SYSTEM.getConfig().getOptions(id)).ifPresent(options -> {
                    int color = options.color();
                    BloodDripParticleOptions particleOptions = new BloodDripParticleOptions(color);
                    S2C_MakeParticles particles = new S2C_MakeParticles(particleOptions, origin.getX(), origin.getEyeY(), origin.getZ(), true, true, Collections.singletonList(Vec3.ZERO));
                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(origin, particles);
                });
            }
            if (drained.isEmpty() && !drained.refillable()) {
                itemStack.shrink(1);
                return false;
            } else {
                updateBloodContainer(itemStack, drained);
            }
            return !container.isEmpty();
        }
        return true;
    }

    @Override
    protected ItemStack finishInteraction(ItemStack itemStack, InteractionTarget interaction, LivingEntity target, LivingEntity origin) {
        BloodContainer container = getBloodContainer(itemStack);
        EntityBloodSystem bloodSystem = EntityBloodSystem.getAttached(target);
        if (bloodSystem == null)
            return itemStack;
        EntityBloodSystemDefinition definition = bloodSystem.getDefinition();
        BloodContainerMode mode = getActiveMode(itemStack);
        if (mode == BloodContainerMode.TRANSFUSION) {
            float containerExtractionAmount = Math.min(container.value(), bloodSystem.getMissingBloodVolumeForTransfusion());
            ResourceLocation bloodType = container.bloodType().orElse(bloodSystem.getBloodType());
            float transfusionAmount = bloodSystem.performTransfusion(target, containerExtractionAmount, bloodType);
            BloodContainer updatedContainer = container.extract(transfusionAmount);
            if (updatedContainer.isEmpty() && !updatedContainer.refillable()) {
                itemStack.shrink(1);
            } else {
                updateBloodContainer(itemStack, updatedContainer);
            }
        } else if (container.refillable() && mode == BloodContainerMode.EXTRACTION) {
            float fillAmount = Math.min(container.getMissingCapacity(), definition.getMaxBloodVolume());
            float extracted = bloodSystem.extractBlood(fillAmount);
            BloodContainer refilledContainer = container.fill(extracted, bloodSystem.getBloodType());
            updateBloodContainer(itemStack, refilledContainer);
        }

        return itemStack;
    }

    @Override
    protected @Nullable Component getInteractionLabel(ItemStack itemStack, InteractionTarget interaction, LivingEntity target, LivingEntity origin, int time, boolean infinite) {
        BloodContainerMode mode = getActiveMode(itemStack);
        return infinite ? mode.getActionLabel() : mode.getActionLabel(formatUsageDuration(time));
    }

    @Override
    protected boolean canInteractWithEntity(ItemStack stack, LivingEntity entity, LivingEntity origin) {
        BloodContainerMode mode = getActiveMode(stack);
        return !mode.isDraining() && BloodSystemManager.isEnabled(entity);
    }

    @Override
    public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        BloodContainerMode mode = getActiveMode(stack);
        Component label = mode.getLabel().plainCopy().withStyle(ChatFormatting.GREEN);
        Component modeLabel = Component.translatable("label.medsystem.blood_container.mode", label).withStyle(ChatFormatting.GRAY);
        tooltipComponents.add(modeLabel);

        BloodContainer container = getBloodContainer(stack);
        if (container == null) {
            return;
        }
        Component value = Component.translatable("label.medsystem.unit.liter", String.format(Locale.ROOT, "%.2f", container.value())).withStyle(ChatFormatting.RED);
        Component capacity = Component.translatable("label.medsystem.unit.liter", String.format(Locale.ROOT, "%.2f", container.capacity())).withStyle(ChatFormatting.RED);
        tooltipComponents.add(Component.translatable("label.medsystem.blood", value, capacity).withStyle(ChatFormatting.GRAY));
        if (!container.isEmpty()) {
            Component typeLabel = container.getBloodTypeLabel();
            Component bloodType = Component.translatable("label.medsystem.blood_type", typeLabel).withStyle(ChatFormatting.GRAY);
            tooltipComponents.add(bloodType);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        BloodContainerMode mode = getActiveMode(stack);
        return mode.isDraining()
                ? 72000
                : Duration.seconds(10).tickValue();
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    protected float getDrainAmount(ItemStack itemStack) {
        return 0.05F;
    }

    private static BloodContainerMode getActiveMode(ItemStack itemStack) {
        BloodContainer container = getBloodContainer(itemStack);
        BloodContainerMode defaultMode = container.refillable() ? BloodContainerMode.EXTRACTION : BloodContainerMode.TRANSFUSION;
        return itemStack.getOrDefault(MedSystemItemComponents.BLOOD_CONTAINER_MODE, defaultMode);
    }

    private static BloodContainer getBloodContainer(ItemStack itemStack) {
        return itemStack.get(MedSystemItemComponents.BLOOD_CONTAINER);
    }

    private static void updateBloodContainer(ItemStack itemStack, BloodContainer container) {
        itemStack.set(MedSystemItemComponents.BLOOD_CONTAINER, container);
    }
}
