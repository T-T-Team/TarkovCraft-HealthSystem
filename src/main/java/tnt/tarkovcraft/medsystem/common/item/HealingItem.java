package tnt.tarkovcraft.medsystem.common.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import tnt.tarkovcraft.core.common.data.Duration;
import tnt.tarkovcraft.core.common.data.TickValue;
import tnt.tarkovcraft.core.util.helper.TextHelper;
import tnt.tarkovcraft.medsystem.api.HealAttributes;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;
import tnt.tarkovcraft.medsystem.network.message.S2C_OpenBodyPartSelectScreen;

// TODO make this a normal class to be used by all healing items, all should be controlled by the heal attributes only
public class HealingItem extends Item {

    private final TickValue useTime;
    private final ItemUseAnimation useAnimation;

    public HealingItem(TickValue useTime, ItemUseAnimation animation, Properties properties) {
        super(properties);
        this.useTime = useTime;
        this.useAnimation = animation;
    }

    public HealingItem(int useTime, ItemUseAnimation animation, Properties properties) {
        this(Duration.ticks(useTime), animation, properties);
    }

    public HealingItem(TickValue useTime, Properties properties) {
        this(useTime, ItemUseAnimation.BOW, properties);
    }

    public HealingItem(int useTime, Properties properties) {
        this(Duration.ticks(useTime), properties);
    }

    protected boolean allowPartialUse() {
        return true;
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (!this.canUseItem(stack, livingEntity)) {
            livingEntity.stopUsingItem();
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        String targetLimb = this.getTargetLimb(stack);
        if (!this.canUseItem(stack, livingEntity) || TextHelper.isBlank(targetLimb)) {
            return stack; // TODO error
        }
        HealAttributes attributes = stack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
        HealthContainer container = HealthSystem.getHealthData(livingEntity);
        int limit = this.getMaxUseAmount(stack);
        int consume = this.getConsumeAmount(stack, limit); // TODO remove, instead go action by action
        float consumptionFactor = consume / (float) limit; // more precise healing amount

        // dead limb recovery
        if (attributes.canHealDeadLimbs()) {
            HealAttributes.DeadLimbHealing deadLimbHealing = attributes.deadLimbHealing();
            BodyPart part = container.getBodyPart(targetLimb);
            if (part.isDead()) {
                part.setHealth(deadLimbHealing.healthAfterHeal());
                deadLimbHealing.applyPost(livingEntity, container);
            }
        }
        if (!level.isClientSide()) {
            stack.hurtAndBreak(consume, (ServerLevel) level, livingEntity, item -> livingEntity.onEquippedItemBroken(item, EquipmentSlot.MAINHAND));
        }
        stack.remove(MedSystemItemComponents.SELECTED_BODY_PART);
        container.updateHealth(livingEntity);
        HealthSystem.synchronizeEntity(livingEntity);
        return stack;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (this.canUseItem(stack, player)) {
            String selectedBodyPart = this.getSelectedBodyPart(stack);
            if (!player.isCrouching() && selectedBodyPart != null && player.getData(MedSystemDataAttachments.HEALTH_CONTAINER).hasBodyPart(selectedBodyPart)) {
                player.startUsingItem(hand);
                return InteractionResult.SUCCESS;
            } else {
                if (!level.isClientSide()) {
                    PacketDistributor.sendToPlayer((ServerPlayer) player, new S2C_OpenBodyPartSelectScreen());
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 30;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return this.useAnimation;
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    public final String getSelectedBodyPart(ItemStack stack) {
        return stack.get(MedSystemItemComponents.SELECTED_BODY_PART);
    }

    protected boolean checkDurability(ItemStack stack, int durabilityUse) {
        int consume = this.getConsumeAmount(stack, durabilityUse);
        if (consume < durabilityUse && !this.allowPartialUse()) {
            return false;
        }
        return consume > 0;
    }

    protected int getConsumeAmount(ItemStack stack, int requestedAmount) {
        int damage = stack.getDamageValue();
        int maxDamage = stack.getMaxDamage();
        int diff = maxDamage - damage;
        return Math.min(requestedAmount, diff);
    }

    public boolean canUseItem(ItemStack stack, LivingEntity entity) {
        if (!HealthSystem.hasCustomHealth(entity)) {
            return false;
        }
        if (!stack.has(MedSystemItemComponents.HEAL_ATTRIBUTES)) {
            return false;
        }
        HealAttributes attributes = stack.get(MedSystemItemComponents.HEAL_ATTRIBUTES);
        return attributes.canUseOn(entity, HealthSystem.getHealthData(entity));
    }

    protected int getMaxUseAmount(ItemStack stack) {
        return this.getMaxUseAmount(stack.get(MedSystemItemComponents.HEAL_ATTRIBUTES));
    }

    protected int getMaxUseAmount(HealAttributes attributes) {
        return attributes != null ? attributes.getConsumption() : Integer.MAX_VALUE;
    }

    protected String getTargetLimb(ItemStack stack) {
        return stack.get(MedSystemItemComponents.SELECTED_BODY_PART);
    }
}
