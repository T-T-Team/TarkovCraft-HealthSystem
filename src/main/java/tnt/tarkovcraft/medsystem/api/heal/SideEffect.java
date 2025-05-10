package tnt.tarkovcraft.medsystem.api.heal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import tnt.tarkovcraft.core.common.attribute.Attribute;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.common.data.duration.DurationFormats;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.EffectType;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemAttributes;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.Consumer;

public record SideEffect(float chance, int duration, int delay, Holder<StatusEffectType<?>> effect) implements TooltipProvider {

    public static final Codec<SideEffect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.floatRange(0.0F, 1.0F).optionalFieldOf("chance", 1.0F).forGetter(t -> t.chance),
            Codec.INT.optionalFieldOf("duration", 1200).forGetter(t -> t.duration),
            Codec.INT.optionalFieldOf("delay", 0).forGetter(t -> t.delay),
            MedSystemRegistries.STATUS_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(t -> t.effect)
    ).apply(instance, SideEffect::new));

    public void apply(LivingEntity entity, HealthContainer container, @Nullable BodyPart part) {
        this.applyFromDamage(entity, null, container, part);
    }

    public void applyFromDamage(LivingEntity entity, @Nullable DamageSource damageSource, HealthContainer container, @Nullable BodyPart part) {
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
            StatusEffectMap effects = type.isGlobalEffect() ? container.getGlobalStatusEffects() : part.getStatusEffects();
            StatusEffect statusEffect = this.delay > 0 ? type.createDelayedEffect(duration, delay) : type.createImmediateEffect(duration);
            if (damageSource != null) {
                Entity cause = damageSource.isDirect() ? damageSource.getDirectEntity() : damageSource.getEntity();
                if (cause != null) {
                    statusEffect.setCausingEntity(cause.getUUID());
                }
            }
            effects.addEffect(statusEffect);
            if (this.delay > 0) {
                effects.addEffect(type.createDelayedEffect(duration, this.delay));
            } else {
                effects.addEffect(type.createImmediateEffect(duration));
            }
        }
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter) {
        StatusEffectType<?> type = effect.value();
        EffectType effectType = type.getEffectType();
        MutableComponent component = Component.literal(" - ");
        if (chance < 1.0F) {
            component.append(String.format(Locale.ROOT, "%.1f%%", chance * 100) + " ");
        }
        component.append(type.getDisplayName());
        if (duration > 0) {
            component.append(" / ").append(Component.translatable("tooltip.medsystem.heal_attributes.side_effects.duration", Duration.format(duration, DurationFormats.SHORT_NAME)));
        }
        if (delay > 0) {
            component.append(" / ")
                    .append(Component.translatable("tooltip.medsystem.heal_attributes.side_effects.delay", Duration.format(delay, DurationFormats.SHORT_NAME)));
        }
        tooltipAdder.accept(component.withStyle(effectType));
    }
}
