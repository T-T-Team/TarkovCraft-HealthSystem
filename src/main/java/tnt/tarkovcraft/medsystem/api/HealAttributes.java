package tnt.tarkovcraft.medsystem.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import tnt.tarkovcraft.core.common.attribute.Attribute;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.data.Duration;
import tnt.tarkovcraft.core.common.data.TickValue;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextImpl;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.MedicalSystemContextKeys;
import tnt.tarkovcraft.medsystem.common.effect.*;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemAttributes;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;
import tnt.tarkovcraft.medsystem.common.item.HealingItem;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public record HealAttributes(boolean applyGlobally, int minUseTime, DeadLimbHealing deadLimbHealing, HealthRecovery health, List<EffectRecovery> recoveries, List<SideEffect> sideEffects) implements TooltipProvider {

    public static final Codec<HealAttributes> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("applyGlobally", true).forGetter(HealAttributes::applyGlobally),
            Codec.INT.optionalFieldOf("minUseTime", 20).forGetter(HealAttributes::minUseTime),
            DeadLimbHealing.CODEC.optionalFieldOf("deadLimbHeal").forGetter(t -> Optional.ofNullable(t.deadLimbHealing)),
            HealthRecovery.CODEC.optionalFieldOf("health").forGetter(t -> Optional.ofNullable(t.health)),
            EffectRecovery.CODEC.listOf().optionalFieldOf("recovers", Collections.emptyList()).forGetter(HealAttributes::recoveries),
            SideEffect.CODEC.listOf().optionalFieldOf("sideEffects", Collections.emptyList()).forGetter(HealAttributes::sideEffects)
    ).apply(instance, HealAttributes::new));

    private HealAttributes(Builder builder) {
        this(!builder.requiresSpecificBodyPart, builder.minUseTime, builder.deadLimbHealing, builder.healthRecovery, builder.recoveries, builder.sideEffects);
    }

    private HealAttributes(boolean applyGlobally, int minUseTime, Optional<DeadLimbHealing> deadLimbHealing, Optional<HealthRecovery> healthRecovery, List<EffectRecovery> recoveries, List<SideEffect> sideEffects) {
        this(applyGlobally, minUseTime, deadLimbHealing.orElse(null), healthRecovery.orElse(null), recoveries, sideEffects);
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
        if (!this.sideEffects.isEmpty()) {
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

    public boolean canUseOnPart(BodyPart part, LivingEntity entity, ItemStack stack, HealthContainer container) {
        if (!this.sideEffects.isEmpty()) {
            return true;
        }
        if (!this.recoveries.isEmpty()) {
            for (EffectRecovery recovery : this.recoveries) {
                StatusEffectType<?> type = recovery.effect.value();
                StatusEffectMap map = type.isGlobalEffect() ? container.getGlobalStatusEffects() : part.getStatusEffects();
                if (HealingItem.checkDurability(stack, recovery.consumption()) && map.hasEffect(recovery.effect)) {
                    return true;
                }
            }
        }
        if (this.canHealDeadLimbs() && part.isDead()) {
            return true;
        }
        return this.health != null && part.getHealth() < part.getMaxHealth();
    }

    public boolean canHealDeadLimbs() {
        return this.deadLimbHealing != null;
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter) {
        if (this.health != null) {
            Component healthPoints = Component.literal(String.format(Locale.ROOT, "%.1f", this.health.healthPerCycle)).withStyle(ChatFormatting.GREEN);
            Component duration = Duration.format(this.health.cycleDuration).copy().withStyle(ChatFormatting.YELLOW);
            if (this.health.maxCycles > 0) {
                Component healLimit = Component.literal(String.format("%.1f", this.health.healthPerCycle * this.health.maxCycles)).withStyle(ChatFormatting.YELLOW);
                tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.heal.limited", healthPoints, duration, healLimit).withStyle(ChatFormatting.GRAY));
            } else {
                tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.heal.infinite", healthPoints, duration).withStyle(ChatFormatting.GRAY));
            }
        }
        if (this.canHealDeadLimbs()) {
            tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.dead_limb.title").withStyle(ChatFormatting.GRAY));
            Component health = Component.literal(String.valueOf(Mth.ceil(this.deadLimbHealing.healthAfterHeal))).withStyle(ChatFormatting.YELLOW);
            tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.dead_limb.recovery", health).withStyle(ChatFormatting.DARK_GRAY));
            Component maxHealth = Component.literal((int) ((1.0F - this.deadLimbHealing.maxHealthMultiplier) * 100) + "%").withStyle(ChatFormatting.YELLOW);
            if (this.deadLimbHealing.hasPostRecovery()) {
                Component duration = Duration.format(this.deadLimbHealing.recoveryTime).copy().withStyle(ChatFormatting.YELLOW);
                tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.dead_limb.max_health", maxHealth, duration).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        if (!this.recoveries.isEmpty()) {
            tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.recoveries.title").withStyle(ChatFormatting.GRAY));
            this.recoveries.forEach(recovery -> {
                StatusEffectType<?> type = recovery.effect.value();
                MutableComponent recoveryLabel = Component.literal(" - ")
                        .append(Component.translatable("tooltip.medsystem.heal_attributes.recoveries.use_label", String.valueOf(recovery.consumption)))
                        .append(" - ")
                        .append(type.getDisplayName())
                        .withStyle(ChatFormatting.DARK_GRAY);
                tooltipAdder.accept(recoveryLabel);
            });
        }
        if (!this.sideEffects.isEmpty()) {
            tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.side_effects.title").withStyle(ChatFormatting.GRAY));
            this.sideEffects.forEach(effect -> {
                StatusEffectType<?> type = effect.effect.value();
                EffectType effectType = type.getEffectType();
                MutableComponent probability = Component.literal(" - " + String.format(Locale.ROOT, "%.1f%%", effect.chance * 100) + " ")
                        .append(type.getDisplayName())
                        .append(" / ")
                        .append(Duration.format(effect.duration));
                tooltipAdder.accept(probability.withStyle(effectType));
            });
        }
    }

    public record DeadLimbHealing(float healthAfterHeal, float maxHealthMultiplier, float minLimbHealth, int recoveryTime, int useTime) {

        public static final Codec<DeadLimbHealing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("healthAfterHeal", 1.0F).forGetter(DeadLimbHealing::healthAfterHeal),
                ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("maxHealthMultiplier", 1.0F).forGetter(DeadLimbHealing::maxHealthMultiplier),
                Codecs.NON_NEGATIVE_FLOAT.optionalFieldOf("minLimbHealth", 0.0F).forGetter(DeadLimbHealing::minLimbHealth),
                Codecs.NON_NEGATIVE_INT.optionalFieldOf("recoveryTime", 0).forGetter(DeadLimbHealing::recoveryTime),
                Codecs.NON_NEGATIVE_INT.fieldOf("useTime").forGetter(DeadLimbHealing::useTime)
        ).apply(instance, DeadLimbHealing::new));

        public boolean canHeal(LivingEntity entity, HealthContainer container) {
            return container.getBodyPartStream().anyMatch(part -> part.isDead() && part.getMaxHealth() >= this.minLimbHealth);
        }

        public boolean hasPostRecovery() {
            return this.recoveryTime > 0 && this.maxHealthMultiplier < 1.0F;
        }

        public void addRecoveryAttributes(LivingEntity entity, BodyPart part) {
            if (this.hasPostRecovery()) {
                float reductionScale = AttributeSystem.getFloatValue(entity, MedSystemAttributes.INJURY_RECOVERY_AMOUNT, 1.0F);
                float durationScale = AttributeSystem.getFloatValue(entity, MedSystemAttributes.INJURY_RECOVERY_DURATION, 1.0F);
                if (durationScale > 0.0F && reductionScale > 0.0F) {
                    int reduction = Mth.ceil(part.getMaxHealth() * (1.0F - this.maxHealthMultiplier) * reductionScale);
                    int duration = Mth.ceil(Duration.minutes(10).tickValue() * reduction);
                    InjuryRecoveryStatusEffect effect = new InjuryRecoveryStatusEffect(duration, reduction);
                    part.getStatusEffects().addEffect(effect);
                }
            }
        }

        public static class SurgeryBuilder {

            private final Builder parent;
            private float healthAfterHeal = 1.0F;
            private float maxHealthMultiplier = 1.0F;
            private float minLimbHealth = 0.0F;
            private int recoveryTime = 0;
            private int useTime = 100;

            private SurgeryBuilder(Builder parent) {
                this.parent = parent;
            }

            public SurgeryBuilder useTime(int useTime) {
                this.useTime = useTime;
                return this;
            }

            public SurgeryBuilder useTime(TickValue useTime) {
                return this.useTime(useTime.tickValue());
            }

            public SurgeryBuilder recoverHealth(float healthAfterHeal) {
                this.healthAfterHeal = healthAfterHeal;
                return this;
            }

            public SurgeryBuilder minLimbHealth(float minLimbHealth) {
                this.minLimbHealth = minLimbHealth;
                return this;
            }

            public SurgeryBuilder recovery(int recoveryTime, float maxHealthMultiplier) {
                this.recoveryTime = recoveryTime;
                this.maxHealthMultiplier = maxHealthMultiplier;
                return this;
            }

            public SurgeryBuilder recovery(TickValue duration, float maxHealthMultiplier) {
                return this.recovery(duration.tickValue(), maxHealthMultiplier);
            }

            public Builder buildSurgeryAttributes() {
                this.parent.deadLimbHealing = new DeadLimbHealing(this.healthAfterHeal, this.maxHealthMultiplier, this.minLimbHealth, this.recoveryTime, this.useTime);
                return this.parent;
            }
        }
    }

    public record HealthRecovery(int cycleDuration, float healthPerCycle, int maxCycles) {

        public static final Codec<HealthRecovery> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("cycleTime", 20).forGetter(HealthRecovery::cycleDuration),
                ExtraCodecs.POSITIVE_FLOAT.fieldOf("healAmount").forGetter(HealthRecovery::healthPerCycle),
                Codecs.NON_NEGATIVE_INT.optionalFieldOf("maxCycles", 1).forGetter(HealthRecovery::maxCycles)
        ).apply(instance, HealthRecovery::new));

        public int getMaxUseDuration(int itemLimit) {
            return this.maxCycles > 0 ? this.maxCycles * this.cycleDuration : itemLimit;
        }
    }

    public record EffectRecovery(int consumption, Holder<StatusEffectType<?>> effect) {

        public static final Codec<EffectRecovery> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("consumption", 1).forGetter(EffectRecovery::consumption),
                MedSystemRegistries.STATUS_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(EffectRecovery::effect)
        ).apply(instance, EffectRecovery::new));

        public boolean canRecover(HealthContainer container, @Nullable BodyPart part) {
            StatusEffectType<?> type = this.effect.value();
            if (type.isGlobalEffect() && part == null) {
                return false;
            }
            StatusEffectMap effects = type.isGlobalEffect() ? container.getGlobalStatusEffects() : part.getStatusEffects();
            return effects.hasEffect(this.effect);
        }

        public boolean canUse(HealthContainer container) {
            StatusEffectType<?> type = this.effect.value();
            if (type.isGlobalEffect()) {
                return container.getGlobalStatusEffects().hasEffect(this.effect);
            }
            return container.getBodyPartStream().anyMatch(part -> part.getStatusEffects().hasEffect(this.effect));
        }

        public void recover(LivingEntity entity, HealthContainer container, ItemStack stack, @Nullable BodyPart part) {
            StatusEffectType<?> type = this.effect.value();
            StatusEffectMap effects = type.isGlobalEffect() ? container.getGlobalStatusEffects() : part.getStatusEffects();
            Context context = ContextImpl.of(
                    ContextKeys.LIVING_ENTITY, entity,
                    MedicalSystemContextKeys.HEALTH_CONTAINER, container,
                    LootContextParams.TOOL, stack
            );
            effects.remove(this.effect.value(), context);
        }
    }

    public record SideEffect(float chance, int duration, int power, Holder<StatusEffectType<?>> effect) {

        public static final Codec<SideEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(t -> t.chance),
                Codec.INT.optionalFieldOf("duration", 1200).forGetter(t -> t.duration),
                Codec.INT.optionalFieldOf("power", 1).forGetter(t -> t.power),
                MedSystemRegistries.STATUS_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(t -> t.effect)
        ).apply(instance, SideEffect::new));

        public void apply(LivingEntity entity, HealthContainer container, @Nullable BodyPart part) {
            RandomSource source = entity.getRandom();
            StatusEffectType<?> type = this.effect.value();
            Holder<Attribute> chanceAttribute = type.getEffectType().byValue(MedSystemAttributes.POSITIVE_EFFECT_CHANCE, MedSystemAttributes.NEGATIVE_EFFECT_CHANCE, null);
            float effectChance = chanceAttribute != null ? this.chance * AttributeSystem.getFloatValue(entity, chanceAttribute, 1.0F) : this.chance;
            if (source.nextFloat() < effectChance) {
                if (!type.isGlobalEffect() && part == null) {
                    MedicalSystem.LOGGER.error(MedicalSystem.MARKER, "Failed to apply side effect {} as effect is not set as global, but target body part was not provided", effect);
                    return;
                }
                Holder<Attribute> durationAttribute = type.getEffectType().byValue(MedSystemAttributes.POSITIVE_EFFECT_DURATION, MedSystemAttributes.NEGATIVE_EFFECT_DURATION, null);
                int duration = durationAttribute != null ? Mth.ceil(AttributeSystem.getFloatValue(entity, durationAttribute, 1.0F) * this.duration) : this.duration;
                StatusEffect statusEffect = type.createInstance(duration, this.power);
                StatusEffectMap effects = type.isGlobalEffect() ? container.getGlobalStatusEffects() : part.getStatusEffects();
                effects.addEffect(statusEffect);
            }
        }
    }

    public static final class Builder {

        private boolean requiresSpecificBodyPart = true;
        private int minUseTime = 20;
        private DeadLimbHealing deadLimbHealing;
        private HealthRecovery healthRecovery;
        private final List<EffectRecovery> recoveries = new ArrayList<>();
        private final List<SideEffect> sideEffects = new ArrayList<>();

        private Builder() {
        }

        public Builder setNoBodyPartSelection() {
            this.requiresSpecificBodyPart = false;
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

        public Builder sideEffect(float chance, int duration, int power, Holder<StatusEffectType<?>> effect) {
            this.sideEffects.add(new SideEffect(chance, duration, power, effect));
            return this;
        }

        public Builder sideEffect(float chance, TickValue duration, int power, Holder<StatusEffectType<?>> effect) {
            return this.sideEffect(chance, duration.tickValue(), power, effect);
        }

        public Builder sideEffect(float chance, int duration, Holder<StatusEffectType<?>> effect) {
            return this.sideEffect(chance, duration, 0, effect);
        }

        public Builder sideEffect(float chance, TickValue duration, Holder<StatusEffectType<?>> effect) {
            return this.sideEffect(chance, duration.tickValue(), effect);
        }

        public HealAttributes build() {
            return new HealAttributes(this);
        }
    }
}
