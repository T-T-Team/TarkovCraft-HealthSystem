package tnt.tarkovcraft.medsystem.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.event.EntityInteractionEvent;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.util.InteractionHelper;

import javax.annotation.Nullable;
import java.util.Locale;

public abstract class InteractableItem extends Item {

    public InteractableItem(Properties properties) {
        super(properties);
    }

    protected static String formatUsageDuration(int time) {
        return String.format(Locale.ROOT, "%.2f", time / 20.0F);
    }

    /**
     * Checks if already initialized interaction is still valid for interaction. Called only when InteractionTarget already exists on
     * the given itemStack
     * @param itemStack Interaction item
     * @param interaction Existing interaction instance
     * @param target Interaction target entity, may be same as origin
     * @param origin Entity who initiated the interaction
     * @return If the existing interaction is valid, returning false will result in interaction cancellation and data removal
     * from the itemStack
     */
    protected abstract boolean tryInitiateExistingInteraction(ItemStack itemStack, InteractionTarget interaction, LivingEntity target, Player origin);

    /**
     * Allows you to implement custom interaction initialization logic - for example show screen, etc
     * @param itemStack Interaction item
     * @param interaction Mutable interaction instance, can be used for additional setup logic
     * @param target Interaction target entity, may be same as origin
     * @param origin Entity who initiated the interaction
     * @return InteractionResult - {@link InteractionResult#SUCCESS} will save the interaction data and start item usage on entity via {@link LivingEntity#startUsingItem(InteractionHand)}
     */
    protected abstract InteractionResult initiateInteraction(ItemStack itemStack, InteractionTarget.Mutable interaction, LivingEntity target, Player origin);

    /**
     * Allows you to implement additional tick-based interaction logic
     * @param level Interaction level
     * @param itemStack Interaction item
     * @param interaction Active interaction instance
     * @param target Interaction target, may be same as origin
     * @param origin Entity who initiated interaction
     * @param remainingUseTicks Remaining interaction ticks based on item use duration
     * @return {@code false} if interaction is no longer valid and should be cancelled. {@code true} proceeds interaction
     */
    protected abstract boolean updateInteraction(Level level, ItemStack itemStack, InteractionTarget interaction, LivingEntity target, LivingEntity origin, int remainingUseTicks);

    /**
     * Handles custom interaction completion logic
     * @param itemStack Interaction item
     * @param interaction Active interaction instance
     * @param target Interaction target, may be same as origin
     * @param origin Entity who initiated interaction
     * @return ItemStack after interaction
     */
    protected abstract ItemStack finishInteraction(ItemStack itemStack, InteractionTarget interaction, LivingEntity target, LivingEntity origin);

    @Override
    public final InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        InteractionTarget activeInteraction = this.getActiveInteraction(itemStack);
        // Existing interaction processing
        if (activeInteraction != null) {
            LivingEntity target = activeInteraction.getTargetLivingEntity(player);
            if (this.canUseItem(itemStack, target, player) && this.tryInitiateExistingInteraction(itemStack, activeInteraction, target, player)) {
                player.startUsingItem(hand);
                return InteractionResultHolder.success(itemStack);
            }
        }
        // Handle initiation of new interaction
        LivingEntity target = this.findInteractiveTarget(itemStack, player);
        if (target == null) {
            this.resetActiveInteraction(itemStack);
            return InteractionResultHolder.fail(itemStack);
        }
        // Initialize
        boolean selfInteraction = target == player;
        InteractionTarget.Mutable interaction = new InteractionTarget.Mutable(selfInteraction, selfInteraction ? 0 : target.getId(), "");
        InteractionResult result = this.initiateInteraction(itemStack, interaction, target, player);
        if (result == InteractionResult.SUCCESS) {
            this.setActiveInteraction(itemStack, interaction.toImmutable());
            player.startUsingItem(hand);
        }
        return switch (result) {
            case CONSUME, CONSUME_PARTIAL -> InteractionResultHolder.consume(itemStack);
            case SUCCESS, SUCCESS_NO_ITEM_USED -> InteractionResultHolder.success(itemStack);
            case FAIL -> InteractionResultHolder.fail(itemStack);
            case PASS -> InteractionResultHolder.pass(itemStack);
        };
    }

    @Override
    public final void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        // Interaction validation
        InteractionTarget interaction = this.getActiveInteraction(stack);
        if (interaction == null) {
            livingEntity.stopUsingItem();
            return;
        }
        // Existing target validation - range checks are responsibility of implementations
        LivingEntity target = interaction.getTargetLivingEntity(livingEntity);
        if (target == null) {
            livingEntity.stopUsingItem();
            return;
        }
        // Item usage validation - conditions may change on tick basis, so better to validate each tick
        if (!this.canUseItem(stack, target, livingEntity)) {
            this.cancelInteractionWithCooldown(stack, livingEntity);
            return;
        }
        // Custom interaction tick logic
        boolean interactionTickResult = this.updateInteraction(level, stack, interaction, target, livingEntity, remainingUseDuration);
        if (!interactionTickResult) {
            this.cancelInteractionWithCooldown(stack, livingEntity);
            return;
        }
        // Interaction progress message
        // TODO handle on client side instead?
        if (!level.isClientSide()) {
            boolean infinite = this.getUseDuration(stack, livingEntity) >= 72000;
            Component label = this.getInteractionLabel(stack, interaction, target, livingEntity, remainingUseDuration, infinite);
            if (label != null && livingEntity instanceof Player player) {
                player.displayClientMessage(label, true);
            }
        }
    }

    @Override
    public final void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        InteractionTarget interaction = this.getActiveInteraction(stack);
        this.onInteractionCancelled(stack, interaction, entity, count);
        if (interaction != null && this.shouldClearInteractionDataOnCancellation(stack, entity, count)) {
            this.resetActiveInteraction(stack);
        }
    }

    @Override
    public final ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        InteractionTarget interaction = this.getActiveInteraction(stack);
        LivingEntity target = interaction.getTargetLivingEntity(livingEntity);
        if (target == null) {
            this.resetActiveInteraction(stack);
            return stack;
        }
        if (!this.canUseItem(stack, target, livingEntity)) {
            this.resetActiveInteraction(stack);
            return stack;
        }
        ItemStack result = this.finishInteraction(stack, interaction, target, livingEntity);
        this.resetActiveInteraction(result);
        InteractionHelper.addCooldown(livingEntity, result, 10);
        return result;
    }

    /**
     * Verifies that current itemStack can be used in interaction
     * @param itemStack ItemStack to be verified
     * @param target Interaction target, may be same as origin
     * @param origin Entity who initiated the interaction
     * @return If the held item can be used
     */
    protected boolean canUseItem(ItemStack itemStack, LivingEntity target, LivingEntity origin) {
        if (target != origin && origin.distanceToSqr(target) > 10) {
            return false;
        }
        return !(origin instanceof Player player) || !player.getCooldowns().isOnCooldown(itemStack.getItem());
    }

    protected boolean canInteractWithEntity(ItemStack stack, LivingEntity entity, LivingEntity origin) {
        return true;
    }

    protected boolean shouldClearInteractionDataOnCancellation(ItemStack itemStack, LivingEntity entity, int count) {
        return true;
    }

    protected void onInteractionCancelled(ItemStack itemStack, InteractionTarget activeInteraction, LivingEntity entity, int count) {
    }

    @Nullable
    protected Component getInteractionLabel(ItemStack itemStack, InteractionTarget interaction, LivingEntity target, LivingEntity origin, int time, boolean infinite) {
        return null;
    }

    protected final InteractionTarget getActiveInteraction(ItemStack itemStack) {
        return itemStack.get(MedSystemItemComponents.INTERACTION_TARGET);
    }

    protected final void setActiveInteraction(ItemStack itemStack, InteractionTarget target) {
        itemStack.set(MedSystemItemComponents.INTERACTION_TARGET, target);
    }

    protected final void resetActiveInteraction(ItemStack itemStack) {
        itemStack.remove(MedSystemItemComponents.INTERACTION_TARGET);
    }

    protected final void cancelInteractionWithCooldown(ItemStack itemStack, LivingEntity entity) {
        entity.stopUsingItem();
        this.resetActiveInteraction(itemStack);
        InteractionHelper.addCooldown(entity, itemStack, 10);
    }

    private LivingEntity findInteractiveTarget(ItemStack itemStack, LivingEntity origin) {
        double range = 3.0;
        Vec3 eye = origin.getEyePosition();
        Vec3 look = origin.getViewVector(1.0F);
        Vec3 end = eye.add(look.x * range, look.y * range, look.z * range);
        AABB aabb = origin.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0, 1.0, 1.0);
        HitResult result = ProjectileUtil.getEntityHitResult(origin, eye, end, aabb, EntitySelector.LIVING_ENTITY_STILL_ALIVE, range * range);
        LivingEntity entity = null;
        if (result != null) {
            result = InteractionHelper.filterHitResult(result, eye, range);
        }
        if (result != null && result.getType() == HitResult.Type.ENTITY) {
            entity = (LivingEntity) ((EntityHitResult) result).getEntity();
        }
        if (entity != null && this.canUseItem(itemStack, entity, origin) && this.allowExternalInteraction(itemStack, entity, origin)) {
            return entity;
        }
        if (this.canUseItem(itemStack, origin, origin) && this.isEventInteractionAllowed(itemStack, origin, origin)) {
            return origin;
        }
        return null;
    }

    private boolean isEventInteractionAllowed(ItemStack itemStack, LivingEntity entity, LivingEntity origin) {
        EntityInteractionEvent.CanInteract event = NeoForge.EVENT_BUS.post(new EntityInteractionEvent.CanInteract(itemStack, entity, origin));
        return !event.isCanceled();
    }

    private boolean allowExternalInteraction(ItemStack itemStack, LivingEntity entity, LivingEntity origin) {
        MedSystemConfig config = MedicalSystem.getConfig();
        return config.allowThirdPartyEntityInteractions && this.isEventInteractionAllowed(itemStack, entity, origin) && this.canInteractWithEntity(itemStack, entity, origin);
    }
}
