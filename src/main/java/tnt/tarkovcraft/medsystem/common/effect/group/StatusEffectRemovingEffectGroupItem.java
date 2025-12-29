package tnt.tarkovcraft.medsystem.common.effect.group;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.api.heal.SideEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.EffectType;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectGroupItems;

import java.util.function.Consumer;

public record StatusEffectRemovingEffectGroupItem(TagKey<StatusEffectType<?>> tag, EffectType classification,
                                                  Component label) implements EffectGroupItem {

    public static final MapCodec<StatusEffectRemovingEffectGroupItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TagKey.codec(MedSystemRegistries.Keys.STATUS_EFFECT).fieldOf("tag").forGetter(t -> t.tag),
            EffectType.CODEC.fieldOf("classification").forGetter(t -> t.classification),
            ComponentSerialization.CODEC.fieldOf("label").forGetter(t -> t.label)
    ).apply(instance, StatusEffectRemovingEffectGroupItem::new));

    @Override
    public void init(EffectGroupHolder holder, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
    }

    @Override
    public void apply(EffectGroupHolder holder, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        Level level = entity.level();
        long time = level.getGameTime();
        if (time % 20L != 0L) {
            return;
        }
        // could be possibly restricted to specific limb, unspecified would clear all effects
        if (container.removeMatchingStatusEffects(this.tag, entity)) {
            HealthSystem.synchronizeEntity(entity);
        }
    }

    @Override
    public void cleanup(EffectGroupHolder holder, HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
    }

    @Override
    public void addInformation(EffectGroupHolder holder, Consumer<Component> tooltip, boolean isItemTooltip) {
        Component component;
        if (isItemTooltip) {
            component = SideEffect.createDescriptionComponent(this.classification, this.label, 1.0F, holder.getDuration(), holder.getDelay());
        } else {
            component = this.label.copy()
                    .append(" ").append(StatusEffect.getDurationLabel(holder.getDuration()))
                    .withStyle(ChatFormatting.DARK_GRAY);
        }
        tooltip.accept(component);
    }

    @Override
    public EffectGroupItem copy() {
        return new StatusEffectRemovingEffectGroupItem(this.tag, this.classification, this.label);
    }

    @Override
    public EffectGroupHolder tryToMergeWith(EffectGroupHolder current, EffectGroupHolder other) {
        return null;
    }

    @Override
    public EffectGroupItemType<?> getType() {
        return MedSystemStatusEffectGroupItems.STATUS_EFFECT_REMOVING.value();
    }
}
