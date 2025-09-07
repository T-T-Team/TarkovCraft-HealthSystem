package tnt.tarkovcraft.medsystem.api.heal;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public record SideEffectHolder(Optional<Component> title, List<SideEffect> sideEffects, boolean hideTooltip) implements TooltipProvider {

    public static final Component DEFAULT_TITLE = Component.translatable("tooltip.medsystem.heal_attributes.side_effects.title").withStyle(ChatFormatting.GRAY);
    public static final Component ITEM_TITLE = Component.translatable("tooltip.medsystem.heal_attributes.side_effects.title_item").withStyle(ChatFormatting.GRAY);
    public static final Component USAGE_TITLE = Component.translatable("tooltip.medsystem.heal_attributes.side_effects.title_usage").withStyle(ChatFormatting.GRAY);
    public static final MapCodec<SideEffectHolder> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ComponentSerialization.CODEC.optionalFieldOf("title").forGetter(t -> t.title),
            SideEffect.CODEC.listOf().fieldOf("effects").forGetter(t -> t.sideEffects),
            Codec.BOOL.optionalFieldOf("hideTooltip", false).forGetter(t -> t.hideTooltip)
    ).apply(instance, SideEffectHolder::new));
    public static final Codec<SideEffectHolder> CODEC = MAP_CODEC.codec();

    public static Builder builder() {
        return new Builder();
    }

    public void apply(LivingEntity target, HealthContainer container, @Nullable BodyPart part) {
        this.applyFromDamage(target, null, container, part);
    }

    public void applyFromDamage(LivingEntity target, @Nullable DamageSource source, HealthContainer container, @Nullable BodyPart part) {
        MedSystemConfig config = MedicalSystem.getConfig();
        if (!config.statusEffects.enableItemDamageStatusEffects)
            return;
        for (SideEffect effect : sideEffects) {
            effect.applyFromDamage(target, source, container, part);
        }
    }

    public static SideEffectHolder fromDamage(DamageSource source) {
        if (source.isDirect()) {
            ItemStack stack = source.getWeaponItem();
            if (stack == null || stack.isEmpty()) return null;
            if (!(stack.getItem() instanceof SideEffectProcessor)) {
                return stack.get(MedSystemItemComponents.SIDE_EFFECTS);
            }
        } else {
            Entity projectile = source.getDirectEntity();
            if (projectile != null && projectile.hasData(MedSystemDataAttachments.SIDE_EFFECTS) && !(projectile instanceof SideEffectProcessor)) {
                return projectile.getData(MedSystemDataAttachments.SIDE_EFFECTS);
            }
        }
        return null;
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter) {
        if (this.hideTooltip)
            return;
        Component title = this.title.orElse(DEFAULT_TITLE);
        tooltipAdder.accept(title);
        this.sideEffects.forEach(effect -> effect.addToTooltip(context, tooltipAdder, flag, componentGetter));
    }

    public static final class Builder {

        private Component title;
        private final List<SideEffect> sideEffects = new ArrayList<>();
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
            statusEffect.setDelay(delay);
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

        public SideEffectHolder build() {
            Preconditions.checkState(!sideEffects.isEmpty(), "sideEffects cannot be empty");
            return new SideEffectHolder(Optional.ofNullable(this.title), sideEffects, hideTooltip);
        }
    }
}
