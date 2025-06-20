package tnt.tarkovcraft.medsystem.api.heal;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDataAttachments;
import tnt.tarkovcraft.medsystem.common.init.MedSystemItemComponents;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record SideEffectHolder(List<SideEffect> sideEffects, boolean hideTooltip) implements TooltipProvider {

    public static final MapCodec<SideEffectHolder> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
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
        tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.side_effects.title").withStyle(ChatFormatting.GRAY));
        this.sideEffects.forEach(effect -> effect.addToTooltip(context, tooltipAdder, flag, componentGetter));
    }

    public static final class Builder {

        private final List<SideEffect> sideEffects = new ArrayList<>();
        private boolean hideTooltip = false;

        private Builder() {}

        public Builder noTooltip() {
            this.hideTooltip = true;
            return this;
        }

        public Builder sideEffect(float chance, int duration, int delay, Holder<StatusEffectType<?>> effect) {
            this.sideEffects.add(new SideEffect(chance, duration, delay, effect));
            return this;
        }

        public Builder sideEffect(float chance, TickValue duration, int delay, Holder<StatusEffectType<?>> effect) {
            return this.sideEffect(chance, duration.tickValue(), delay, effect);
        }

        public Builder sideEffect(float chance, int duration, TickValue delay, Holder<StatusEffectType<?>> effect) {
            return this.sideEffect(chance, duration, delay.tickValue(), effect);
        }

        public Builder sideEffect(float chance, TickValue duration, TickValue delay, Holder<StatusEffectType<?>> effect) {
            return this.sideEffect(chance, duration.tickValue(), delay.tickValue(), effect);
        }

        public Builder sideEffect(float chance, int duration, Holder<StatusEffectType<?>> effect) {
            return this.sideEffect(chance, duration, 0, effect);
        }

        public Builder sideEffect(float chance, TickValue duration, Holder<StatusEffectType<?>> effect) {
            return this.sideEffect(chance, duration.tickValue(), effect);
        }

        public Builder infiniteSideEffect(float chance, Holder<StatusEffectType<?>> effect) {
            return this.sideEffect(chance, -1, effect);
        }

        public SideEffectHolder build() {
            Preconditions.checkState(!sideEffects.isEmpty(), "sideEffects cannot be empty");
            return new SideEffectHolder(sideEffects, hideTooltip);
        }
    }
}
