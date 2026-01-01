package tnt.tarkovcraft.medsystem.api.heal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import tnt.tarkovcraft.core.common.attribute.Attribute;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.common.data.duration.DurationFormats;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectHelper;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemAttributes;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.Consumer;

public record SideEffect(float chance, int delay, StatusEffect template) implements TooltipProvider {

    public static final Codec<SideEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(SideEffect::chance),
            Codec.INT.optionalFieldOf("delay", 0).forGetter(SideEffect::delay),
            StatusEffectType.CODEC.fieldOf("template").forGetter(SideEffect::template)
    ).apply(instance, SideEffect::new));

    public void apply(LivingEntity entity, @Nullable DamageSource damageSource, HealthContainer container, @Nullable Limb limb) {
        // Skip ignored effect body parts
        StatusEffectType<?> type = this.template.getType();
        if (limb != null && !type.isGlobalEffect()) {
            if (!limb.canApplyStatusEffect(type)) {
                return;
            }
        }

        RandomSource source = entity.getRandom();
        Holder<Attribute> chanceAttribute = this.chance < 1.0F ? type.getEffectType().byValue(MedSystemAttributes.POSITIVE_EFFECT_CHANCE, MedSystemAttributes.NEGATIVE_EFFECT_CHANCE, null) : null;
        float effectChance = chanceAttribute != null ? this.chance * AttributeSystem.getFloatValue(entity, chanceAttribute, 1.0F) : this.chance;
        if (effectChance >= 1.0F || source.nextFloat() < effectChance) {
            if (!type.isGlobalEffect() && limb == null) {
                MedicalSystem.LOGGER.error(MedicalSystem.MARKER, "Failed to apply side effect {} as effect is not set as global, but target body part was not provided", type);
                return;
            }
            StatusEffectMap effects = type.isGlobalEffect() ? container.getGlobalStatusEffects() : limb.getStatusEffects();
            StatusEffect statusEffect = this.template.copy();
            if (!this.template.isInfinite()) {
                Holder<Attribute> durationAttribute = type.getEffectType().byValue(MedSystemAttributes.POSITIVE_EFFECT_DURATION, MedSystemAttributes.NEGATIVE_EFFECT_DURATION, null);
                int duration = durationAttribute != null ? Mth.ceil(AttributeSystem.getFloatValue(entity, durationAttribute, 1.0F) * this.template.getDuration()) : this.template.getDuration();
                statusEffect.setDuration(duration);
            }
            if (damageSource != null) {
                StatusEffectHelper.setCausingEntityFromSource(statusEffect, damageSource);
            }
            StatusEffectHelper.addEffect(effects, entity, limb, this.delay, statusEffect);
        }
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        if (this.template.hasCustomTooltip()) {
            this.template.addCustomTooltip(tooltipAdder);
        } else {
            StatusEffectType<?> type = this.template.getType();
            EffectType effectType = type.getEffectType();
            Component title = type.getDisplayName(this.template);
            tooltipAdder.accept(createDescriptionComponent(effectType, title, this.chance, this.template.getDuration(), this.delay));
        }
    }

    public static Component createDescriptionComponent(EffectType type, Component name, float chance, int duration, int delay) {
        MutableComponent component = Component.literal("> ");
        if (chance < 1.0F) {
            component.append(String.format(Locale.ROOT, "%.1f%%", chance * 100) + " ");
        }
        component.append(name);
        if (duration > 0) {
            component.append(" / ").append(Component.translatable("tooltip.medsystem.heal_attributes.side_effects.duration", Duration.format(duration, DurationFormats.SHORT_NAME)));
        }
        if (delay > 0) {
            component.append(" / ")
                    .append(Component.translatable("tooltip.medsystem.heal_attributes.side_effects.delay", Duration.format(delay, DurationFormats.SHORT_NAME)));
        }
        return component.withStyle(type);
    }
}
