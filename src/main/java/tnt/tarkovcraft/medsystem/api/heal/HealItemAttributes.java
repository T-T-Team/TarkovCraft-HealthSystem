package tnt.tarkovcraft.medsystem.api.heal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.item.HealingItem;
import tnt.tarkovcraft.medsystem.common.status.BloodData;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public record HealItemAttributes(boolean applyGlobally, boolean alwaysConsumable, int minUseTime, Surgery surgery,
                                 HealthRecovery health, List<EffectRecovery> recoveries, List<ConsumeEffect> effects) implements TooltipProvider {

    public static final Codec<HealItemAttributes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("applyGlobally", true).forGetter(HealItemAttributes::applyGlobally),
            Codec.BOOL.optionalFieldOf("alwaysConsumable", false).forGetter(HealItemAttributes::alwaysConsumable),
            Codec.INT.optionalFieldOf("minUseTime", 20).forGetter(HealItemAttributes::minUseTime),
            Surgery.CODEC.optionalFieldOf("deadLimbHeal").forGetter(t -> Optional.ofNullable(t.surgery)),
            HealthRecovery.CODEC.optionalFieldOf("health").forGetter(t -> Optional.ofNullable(t.health)),
            EffectRecovery.CODEC.listOf().optionalFieldOf("recovers", Collections.emptyList()).forGetter(HealItemAttributes::recoveries),
            ConsumeEffect.CODEC.listOf().optionalFieldOf("consumeEffects", Collections.emptyList()).forGetter(HealItemAttributes::effects)
    ).apply(instance, HealItemAttributes::new));

    private HealItemAttributes(Builder builder) {
        this(!builder.requiresSpecificBodyPart, builder.alwaysConsumable, builder.minUseTime, builder.surgery, builder.healthRecovery, builder.recoveries, builder.effects);
    }

    private HealItemAttributes(boolean applyGlobally, boolean alwaysConsumable, int minUseTime, Optional<Surgery> deadLimbHealing, Optional<HealthRecovery> healthRecovery, List<EffectRecovery> recoveries, List<ConsumeEffect> effects) {
        this(applyGlobally, alwaysConsumable, minUseTime, deadLimbHealing.orElse(null), healthRecovery.orElse(null), recoveries, effects);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static HealItemAttributes withSideEffectsOnly(int minUseTime) {
        return builder().setMinUseTime(minUseTime).setAlwaysConsumable().setNoBodyPartSelection().build();
    }

    public static HealItemAttributes withSideEffectsOnly(TickValue minUseTime) {
        return withSideEffectsOnly(minUseTime.tickValue());
    }

    public int getUseDuration(int max) {
        int duration = 0;
        if (this.surgery != null) {
            duration += surgery.useTime();
        }
        if (this.health != null) {
            duration = this.health.getMaxUseDuration(max);
        }
        return Math.max(duration, this.minUseTime());
    }

    public boolean canUseOn(LivingEntity entity, LivingEntity origin, ItemStack stack, HealthContainer container) {
        if (this.alwaysConsumable) {
            return true;
        }
        if (!this.recoveries.isEmpty()) {
            if (this.recoveries.stream().anyMatch(recovery -> HealingItem.checkDurability(stack, recovery.consumption()) && recovery.canUse(container))) {
                return true;
            }
        }
        if (this.isSurgeryItem() && this.surgery.canHeal(container)) {
            return true;
        }
        if (this.health != null) {
            if (entity.getHealth() < entity.getMaxHealth())
                return true;
            // rescue
            if (origin != entity && entity instanceof Player player) {
                BloodData data = BloodSystem.getBloodData(player);
                BloodData.UnconsciousInfo info = data.getUnconsciousInfo();
                return data.isUnconscious() && info.causesDeath();
            }
        }
        return false;
    }

    public boolean canUseOnLimb(Limb limb, ItemStack stack, HealthContainer container, boolean selfHealing, LivingEntity target) {
        if (this.alwaysConsumable) {
            return true;
        }
        if (!this.recoveries.isEmpty()) {
            for (EffectRecovery recovery : this.recoveries) {
                StatusEffectType<?> type = recovery.effect().value();
                StatusEffectMap map = type.isGlobalEffect() ? container.getGlobalStatusEffects() : limb.getStatusEffects();
                if (HealingItem.checkDurability(stack, recovery.consumption()) && map.hasEffect(recovery.effect())) {
                    return true;
                }
            }
        }
        if (this.isSurgeryItem() && limb.isDead()) {
            return true;
        }
        if (this.health != null) {
            if (!limb.isDead() && limb.getHealth() < limb.getMaxHealth()) {
                return true;
            }
            if (!selfHealing && target instanceof Player player && container.getRootLimb().getLimbCode().equals(limb.getLimbCode())) {
                BloodData bloodData = BloodSystem.getBloodData(player);
                BloodData.UnconsciousInfo info = bloodData.getUnconsciousInfo();
                return bloodData.isUnconscious() && info.causesDeath();
            }
        }
        return false;
    }

    public boolean isSurgeryItem() {
        return this.surgery != null;
    }

    public boolean isRecoveryItem() {
        return !this.recoveries.isEmpty();
    }

    public boolean isHealing() {
        return this.health != null;
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter) {
        if (this.health != null) {
            this.health.addToTooltip(context, tooltipAdder, flag, componentGetter);
        }
        if (this.isSurgeryItem()) {
            tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.dead_limb.title").withStyle(ChatFormatting.GRAY));
            this.surgery.addToTooltip(context, tooltipAdder, flag, componentGetter);
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
        private Surgery surgery;
        private HealthRecovery healthRecovery;
        private final List<EffectRecovery> recoveries = new ArrayList<>();
        private final List<ConsumeEffect> effects = new ArrayList<>();

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

        public Builder surgeryItem(UnaryOperator<Surgery.SurgeryBuilder> builder) {
            Surgery.SurgeryBuilder surgeryBuilder = new Surgery.SurgeryBuilder();
            builder.apply(surgeryBuilder);
            this.surgery = surgeryBuilder.buildSurgeryAttributes();
            return this;
        }

        public Builder healing(int duration, int count, float health) {
            if (duration < 20) {
                throw new IllegalArgumentException("duration must be greater than or equal to 20");
            }
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

        public Builder removesEffect(int cost, Holder<StatusEffectType<?>> effect, boolean extendedTooltip) {
            this.recoveries.add(new EffectRecovery(cost, effect, extendedTooltip));
            return this;
        }

        public Builder removesEffect(int cost, Holder<StatusEffectType<?>> effect) {
            return this.removesEffect(cost, effect, true);
        }

        public Builder removesEffect(Holder<StatusEffectType<?>> effect) {
            return this.removesEffect(1, effect, false);
        }

        public Builder consumeEffect(ConsumeEffect effect) {
            this.effects.add(effect);
            return this;
        }

        public HealItemAttributes build() {
            return new HealItemAttributes(this);
        }
    }
}
