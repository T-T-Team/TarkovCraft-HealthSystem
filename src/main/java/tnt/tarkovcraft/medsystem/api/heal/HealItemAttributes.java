package tnt.tarkovcraft.medsystem.api.heal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.item.HealingItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public record HealItemAttributes(boolean applyGlobally, boolean alwaysConsumable, int minUseTime, DeadLimbHealing deadLimbHealing,
                                 HealthRecovery health, List<EffectRecovery> recoveries) implements TooltipProvider {

    public static final Codec<HealItemAttributes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("applyGlobally", true).forGetter(HealItemAttributes::applyGlobally),
            Codec.BOOL.optionalFieldOf("alwaysConsumable", false).forGetter(HealItemAttributes::alwaysConsumable),
            Codec.INT.optionalFieldOf("minUseTime", 20).forGetter(HealItemAttributes::minUseTime),
            DeadLimbHealing.CODEC.optionalFieldOf("deadLimbHeal").forGetter(t -> Optional.ofNullable(t.deadLimbHealing)),
            HealthRecovery.CODEC.optionalFieldOf("health").forGetter(t -> Optional.ofNullable(t.health)),
            EffectRecovery.CODEC.listOf().optionalFieldOf("recovers", Collections.emptyList()).forGetter(HealItemAttributes::recoveries)
    ).apply(instance, HealItemAttributes::new));

    private HealItemAttributes(Builder builder) {
        this(!builder.requiresSpecificBodyPart, builder.alwaysConsumable, builder.minUseTime, builder.deadLimbHealing, builder.healthRecovery, builder.recoveries);
    }

    private HealItemAttributes(boolean applyGlobally, boolean alwaysConsumable, int minUseTime, Optional<DeadLimbHealing> deadLimbHealing, Optional<HealthRecovery> healthRecovery, List<EffectRecovery> recoveries) {
        this(applyGlobally, alwaysConsumable, minUseTime, deadLimbHealing.orElse(null), healthRecovery.orElse(null), recoveries);
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getUseDuration(int max) {
        int duration = 0;
        if (this.deadLimbHealing != null) {
            duration += deadLimbHealing.useTime();
        }
        if (this.health != null) {
            duration = this.health.getMaxUseDuration(max);
        }
        return Math.max(duration, this.minUseTime());
    }

    public boolean canUseOn(LivingEntity entity, ItemStack stack, HealthContainer container) {
        if (this.alwaysConsumable) {
            return true;
        }
        if (!this.recoveries.isEmpty()) {
            if (this.recoveries.stream().anyMatch(recovery -> HealingItem.checkDurability(stack, recovery.consumption()) && recovery.canUse(container))) {
                return true;
            }
        }
        if (this.canHealDeadLimbs() && this.deadLimbHealing.canHeal(entity, container)) {
            return true;
        }
        return this.health != null && entity.getHealth() < entity.getMaxHealth();
    }

    public boolean canUseOnPart(BodyPart part, ItemStack stack, HealthContainer container) {
        if (this.alwaysConsumable) {
            return true;
        }
        if (!this.recoveries.isEmpty()) {
            for (EffectRecovery recovery : this.recoveries) {
                StatusEffectType<?> type = recovery.effect().value();
                StatusEffectMap map = type.isGlobalEffect() ? container.getGlobalStatusEffects() : part.getStatusEffects();
                if (HealingItem.checkDurability(stack, recovery.consumption()) && map.hasEffect(recovery.effect())) {
                    return true;
                }
            }
        }
        if (this.canHealDeadLimbs() && part.isDead()) {
            return true;
        }
        return this.health != null && part.getHealth() < part.getMaxHealth() && !part.isDead();
    }

    public boolean canHealDeadLimbs() {
        return this.deadLimbHealing != null;
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter) {
        if (this.health != null) {
            this.health.addToTooltip(context, tooltipAdder, flag, componentGetter);
        }
        if (this.canHealDeadLimbs()) {
            tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.dead_limb.title").withStyle(ChatFormatting.GRAY));
            this.deadLimbHealing.addToTooltip(context, tooltipAdder, flag, componentGetter);
        }
        if (!this.recoveries.isEmpty()) {
            tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.recoveries.title").withStyle(ChatFormatting.GRAY));
            this.recoveries.forEach(recovery -> recovery.addToTooltip(context, tooltipAdder, flag, componentGetter));
        }
    }

    public static final class Builder {

        private boolean requiresSpecificBodyPart = true;
        private boolean alwaysConsumable = false;
        private int minUseTime = 20;
        DeadLimbHealing deadLimbHealing;
        private HealthRecovery healthRecovery;
        private final List<EffectRecovery> recoveries = new ArrayList<>();

        private Builder() {
        }

        public Builder setNoBodyPartSelection() {
            this.requiresSpecificBodyPart = false;
            return this;
        }

        public Builder setAlwaysConsumable() {
            this.alwaysConsumable = true;
            return this;
        }

        public Builder setMinUseTime(int minUseTime) {
            this.minUseTime = minUseTime;
            return this;
        }

        public Builder setMinUseTime(TickValue minUseTime) {
            return this.setMinUseTime(minUseTime.tickValue());
        }

        public DeadLimbHealing.SurgeryBuilder surgeryItem() {
            return new DeadLimbHealing.SurgeryBuilder(this);
        }

        public Builder healing(int duration, int count, float health) {
            this.healthRecovery = new HealthRecovery(duration, health, count);
            return this;
        }

        public Builder healing(TickValue duration, int count, float health) {
            return this.healing(duration.tickValue(), count, health);
        }

        public Builder unrestrictedHealing(int duration, float health) {
            return this.healing(duration, 0, health);
        }

        public Builder unrestrictedHealing(TickValue duration, float health) {
            return this.unrestrictedHealing(duration.tickValue(), health);
        }

        public Builder removesEffect(int cost, Holder<StatusEffectType<?>> effect) {
            this.recoveries.add(new EffectRecovery(cost, effect));
            return this;
        }

        public HealItemAttributes build() {
            return new HealItemAttributes(this);
        }
    }
}
