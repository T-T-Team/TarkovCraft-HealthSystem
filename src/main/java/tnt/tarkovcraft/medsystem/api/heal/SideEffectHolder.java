package tnt.tarkovcraft.medsystem.api.heal;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.Level;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.medsystem.common.consume_effect.ConsumeEffect;
import tnt.tarkovcraft.medsystem.common.effect.NegativeEffectsGroup;
import tnt.tarkovcraft.medsystem.common.effect.NeutralEffectsGroup;
import tnt.tarkovcraft.medsystem.common.effect.PositiveEffectsGroup;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.group.EffectGroupHolder;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public record SideEffectHolder(Optional<Component> title, List<SideEffect> sideEffects, List<ConsumeEffect> effects, List<Component> additionalLabels, boolean hideTooltip) implements TooltipProvider {

    public static final Component DEFAULT_TITLE = Component.translatable("tooltip.medsystem.heal_attributes.side_effects.title").withStyle(ChatFormatting.GRAY);
    public static final Component ITEM_TITLE = Component.translatable("tooltip.medsystem.heal_attributes.side_effects.title_item").withStyle(ChatFormatting.GRAY);
    public static final Component USAGE_TITLE = Component.translatable("tooltip.medsystem.heal_attributes.side_effects.title_usage").withStyle(ChatFormatting.GRAY);
    public static final SideEffectHolder EMPTY = new SideEffectHolder(Optional.empty(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true);

    public static final Codec<SideEffectHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.optionalFieldOf("title").forGetter(t -> t.title),
            SideEffect.CODEC.listOf().fieldOf("effects").forGetter(t -> t.sideEffects),
            ConsumeEffect.CODEC.listOf().optionalFieldOf("consume_effects", Collections.emptyList()).forGetter(t -> t.effects),
            ComponentSerialization.CODEC.listOf().optionalFieldOf("additional_labels", Collections.emptyList()).forGetter(t -> t.additionalLabels),
            Codec.BOOL.optionalFieldOf("hide_tooltip", false).forGetter(t -> t.hideTooltip)
    ).apply(instance, SideEffectHolder::new));

    public static Builder builder() {
        return new Builder();
    }

    public static Builder withItemUsage() {
        return builder().title(USAGE_TITLE);
    }

    public static SideEffectHolder empty() {
        return EMPTY;
    }

    public void onConsume(ItemStack itemStack, LivingEntity target, HealthContainer container, @Nullable Limb part) {
        this.apply(target, null, container, part);
        Level level = target.level();
        this.effects.forEach(effect -> effect.apply(level, itemStack, target));
    }

    public void apply(LivingEntity target, @Nullable DamageSource source, HealthContainer container, @Nullable Limb part) {
        for (SideEffect effect : sideEffects) {
            effect.apply(target, source, container, part);
        }
    }

    // TODO add in about to attack event directly instead of relying on backward recognition
    public static SideEffectHolder fromDamage(DamageSource source) {
        if (source.isDirect()) {
            ItemStack stack = source.getWeaponItem();
            if (stack == null || stack.isEmpty()) return null;
            return stack.get(MedSystemItemComponents.SIDE_EFFECTS);
        } else {
            Entity projectile = source.getDirectEntity();
            if (projectile != null && projectile.hasData(MedSystemDataAttachments.SIDE_EFFECTS)) {
                return projectile.getData(MedSystemDataAttachments.SIDE_EFFECTS);
            }
        }
        return null;
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag tooltipFlag) {
        if (this.hideTooltip)
            return;
        Component title = this.title.orElse(DEFAULT_TITLE);
        this.additionalLabels.forEach(tooltipAdder);
        tooltipAdder.accept(title);
        this.sideEffects.forEach(effect -> effect.addToTooltip(context, tooltipAdder, tooltipFlag));
    }

    public static final class Builder {

        private Component title;
        private final List<SideEffect> sideEffects = new ArrayList<>();
        private final List<ConsumeEffect> consumeEffects = new ArrayList<>();
        private final List<Component> additionalLabels = new ArrayList<>();
        private boolean hideTooltip = false;

        private Builder() {}

        public Builder title(Component title) {
            this.title = title;
            return this;
        }

        public Builder noTooltip() {
            this.hideTooltip = true;
            return this;
        }

        public Builder delayed(float chance, int duration, int delay, StatusEffect effect) {
            StatusEffect statusEffect = effect.copy();
            statusEffect.setDuration(duration);
            this.sideEffects.add(new SideEffect(chance, delay, statusEffect));
            return this;
        }

        public Builder delayed(float chance, TickValue duration, int delay, StatusEffect effect) {
            return this.delayed(chance, duration.tickValue(), delay, effect);
        }

        public Builder delayed(float chance, int duration, TickValue delay, StatusEffect effect) {
            return this.delayed(chance, duration, delay.tickValue(), effect);
        }

        public Builder delayed(float chance, TickValue duration, TickValue delay, StatusEffect effect) {
            return this.delayed(chance, duration.tickValue(), delay.tickValue(), effect);
        }

        public Builder immediate(float chance, int duration, StatusEffect effect) {
            return this.delayed(chance, duration, 0, effect);
        }

        public Builder immediate(float chance, TickValue duration, StatusEffect effect) {
            return this.immediate(chance, duration.tickValue(), effect);
        }

        public Builder delayed(TickValue duration, TickValue delay, StatusEffect effect) {
            return this.delayed(1.0F, duration.tickValue(), delay.tickValue(), effect);
        }

        public Builder delayed(int duration, TickValue delay, StatusEffect effect) {
            return this.delayed(1.0F, duration, delay.tickValue(), effect);
        }

        public Builder delayed(TickValue duration, int delay, StatusEffect effect) {
            return this.delayed(1.0F, duration.tickValue(), delay, effect);
        }

        public Builder delayed(int duration, int delay, StatusEffect effect) {
            return this.delayed(1.0F, duration, delay, effect);
        }

        public Builder immediate(TickValue duration, StatusEffect effect) {
            return this.immediate(1.0F, duration.tickValue(), effect);
        }

        public Builder infinite(float chance, StatusEffect effect) {
            return this.immediate(chance, -1, effect);
        }

        public Builder infinite(StatusEffect effect) {
            return this.immediate(1.0F, -1, effect);
        }

        public Builder infiniteDelayed(float chance, int delay, StatusEffect effect) {
            return this.delayed(chance, -1, delay, effect);
        }

        public Builder infiniteDelayed(float chance, TickValue delay, StatusEffect effect) {
            return this.infiniteDelayed(chance, delay.tickValue(), effect);
        }

        public Builder infiniteDelayed(TickValue delay, StatusEffect effect) {
            return this.infiniteDelayed(delay.tickValue(), effect);
        }

        public Builder infiniteDelayed(int delay, StatusEffect effect) {
            return this.delayed(1.0F, -1, delay, effect);
        }

        public Builder buffs(Consumer<EffectGroupHolder.Factory> builder) {
            return this.infinite(PositiveEffectsGroup.createTemplate(builder));
        }

        public Builder neutral(Consumer<EffectGroupHolder.Factory> builder) {
            return this.infinite(NeutralEffectsGroup.createTemplate(builder));
        }

        public Builder debuffs(Consumer<EffectGroupHolder.Factory> builder) {
            return this.infinite(NegativeEffectsGroup.createTemplate(builder));
        }

        public Builder consumeEffect(ConsumeEffect effect) {
            this.consumeEffects.add(effect);
            return this;
        }

        public Builder label(Component label) {
            this.additionalLabels.add(label);
            return this;
        }

        public <E extends ConsumeEffect> Builder consumeEffectWithLabel(E effect, Function<E, Component> label) {
            return this.consumeEffect(effect).label(label.apply(effect));
        }

        public SideEffectHolder build() {
            Preconditions.checkState(!sideEffects.isEmpty(), "sideEffects cannot be empty");
            return new SideEffectHolder(Optional.ofNullable(this.title), this.sideEffects, this.consumeEffects, this.additionalLabels, this.hideTooltip);
        }
    }
}
