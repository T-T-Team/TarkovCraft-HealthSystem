package tnt.tarkovcraft.medsystem.common.effect.group;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.attribute.Attribute;
import tnt.tarkovcraft.core.common.attribute.AttributeInstance;
import tnt.tarkovcraft.core.common.attribute.AttributeSystem;
import tnt.tarkovcraft.core.common.attribute.EntityAttributeData;
import tnt.tarkovcraft.core.common.attribute.modifier.AttributeModifier;
import tnt.tarkovcraft.core.common.attribute.modifier.AttributeModifierType;
import tnt.tarkovcraft.core.common.init.CoreRegistries;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.api.heal.SideEffect;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectType;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectGroupItems;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class AttributeModifierEffectGroupItem implements EffectGroupItem {

    public static final MapCodec<AttributeModifierEffectGroupItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            CoreRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(t -> t.attribute),
            AttributeModifierType.ID_CODEC.fieldOf("modifier").forGetter(t -> t.modifier),
            Codecs.enumCodec(EffectType.class).fieldOf("classification").forGetter(t -> t.classification),
            ComponentSerialization.CODEC.fieldOf("valueLabel").forGetter(t -> t.valueLabel)
    ).apply(instance, AttributeModifierEffectGroupItem::new));

    private final Holder<Attribute> attribute;
    private final AttributeModifier modifier;
    private final EffectType classification;
    private final Component valueLabel;

    public AttributeModifierEffectGroupItem(Holder<Attribute> attribute, AttributeModifier modifier, EffectType classification, Component valueLabel) {
        this.attribute = attribute;
        this.modifier = modifier;
        this.classification = classification;
        this.valueLabel = valueLabel;
    }

    @Override
    public void apply(EffectGroupHolder holder, HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
    }

    @Override
    public void init(EffectGroupHolder holder, HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
        if (!AttributeSystem.isEnabledForEntity(entity))
            return;
        EntityAttributeData attributeData = AttributeSystem.getAttributes(entity);
        AttributeInstance instance = attributeData.getAttribute(this.attribute);
        instance.addModifier(this.modifier);
        AttributeSystem.sync(entity);
    }

    @Override
    public void cleanup(EffectGroupHolder holder, HealthContainer container, LivingEntity entity, @Nullable BodyPart limb) {
        if (!AttributeSystem.isEnabledForEntity(entity))
            return;
        EntityAttributeData attributeData = AttributeSystem.getAttributes(entity);
        AttributeInstance instance = attributeData.getAttribute(this.attribute);
        instance.removeModifier(this.modifier);
        AttributeSystem.sync(entity);
    }

    @Override
    public void addInformation(EffectGroupHolder holder, Consumer<Component> tooltip, boolean isItemTooltip) {
        if (isItemTooltip) {
            // > [attributeDisplayName] (<valueLabel>) / Dur.: <duration> / Del.: <delay>
            MutableComponent component = this.attribute.value().getDisplayName().copy().append(" (").append(this.valueLabel).append(")");
            tooltip.accept(SideEffect.createDescriptionComponent(this.classification, component, 1.0F, holder.getDuration(), holder.getDelay()));
        } else {
            // [attributeDisplayName] (<valueLabel>) <duration>
            MutableComponent component = this.attribute.value().getDisplayName().copy()
                            .append(" (").append(this.valueLabel).append(") ").append(StatusEffect.getDurationLabel(holder.getDuration()));
            tooltip.accept(component.withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @Override
    public EffectGroupItem copy() {
        return new AttributeModifierEffectGroupItem(this.attribute, this.modifier, this.classification, this.valueLabel);
    }

    @Override
    public EffectGroupHolder tryToMergeWith(EffectGroupHolder current, EffectGroupHolder other) {
        EffectGroupItem otherItem = other.getItem();
        if (otherItem instanceof AttributeModifierEffectGroupItem attributeGroupItem && attributeGroupItem.attribute.value() == this.attribute.value() && attributeGroupItem.modifier.identifier().equals(this.modifier.identifier())) {
            int delay = Math.min(current.getDelay(), other.getDelay());
            int durationDiff = Mth.abs(current.getDuration() - other.getDuration());
            int newDuration = current.getDuration() + durationDiff / 2;
            return new EffectGroupHolder(current.getItem().copy(), newDuration, delay);
        }
        return null;
    }

    @Override
    public EffectGroupItemType<?> getType() {
        return MedSystemStatusEffectGroupItems.ATTRIBUTE.value();
    }
}
