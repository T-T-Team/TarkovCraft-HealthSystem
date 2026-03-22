package tnt.tarkovcraft.medsystem.api.heal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import tnt.tarkovcraft.medsystem.api.heal.predicate.AnyEffectPredicate;
import tnt.tarkovcraft.medsystem.api.heal.predicate.StatusEffectPredicate;
import tnt.tarkovcraft.medsystem.api.heal.predicate.StatusEffectPredicateType;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.effect.util.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.LimbContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public record EffectRecovery(int consumption, Holder<StatusEffectType<?>> effect, StatusEffectPredicate predicate, Component displayName, boolean extendedTooltip) implements TooltipProvider {

    public static final Codec<EffectRecovery> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("consumption", 1).forGetter(EffectRecovery::consumption),
            MedSystemRegistries.STATUS_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(EffectRecovery::effect),
            StatusEffectPredicateType.CODEC.optionalFieldOf("predicate", AnyEffectPredicate.INSTANCE).forGetter(EffectRecovery::predicate),
            ComponentSerialization.CODEC.optionalFieldOf("display_name", CommonComponents.EMPTY).forGetter(EffectRecovery::displayName),
            Codec.BOOL.optionalFieldOf("extended_tooltip", true).forGetter(EffectRecovery::extendedTooltip)
    ).apply(instance, EffectRecovery::new));

    public boolean canRecover(HealthContainer container, @Nullable Limb part) {
        StatusEffectType<?> type = this.effect.value();
        if (type.isGlobalEffect() && part == null) {
            return false;
        }
        StatusEffectMap effects = type.isGlobalEffect() ? container.getGlobalStatusEffects() : part.getStatusEffects();
        return this.findEffect(effects).isPresent();
    }

    public boolean canUse(HealthContainer container) {
        StatusEffectType<?> type = this.effect.value();
        if (type.isGlobalEffect()) {
            return this.findEffect(container.getGlobalStatusEffects()).isPresent();
        }
        LimbContainer limbContainer = container.getLimbContainer();
        return limbContainer.hasLimb(limb -> this.findEffect(limb.getStatusEffects()).isPresent());
    }

    public void recover(HealthContainer container, @Nullable Limb part) {
        StatusEffectType<?> type = this.effect.value();
        StatusEffectMap effects = type.isGlobalEffect() ? container.getGlobalStatusEffects() : part.getStatusEffects();
        this.findEffect(effects)
                .ifPresent(StatusEffect::markForRemoval);
    }

    @Nullable
    public Optional<StatusEffect> findEffect(StatusEffectMap map) {
        Optional<StatusEffect> holder = map.getEffect(this.effect);
        return holder.filter(this.predicate);
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        StatusEffectType<?> type = effect.value();
        MutableComponent recoveryLabel = Component.literal("> ");
        if (this.extendedTooltip) {
            recoveryLabel.append(Component.translatable("tooltip.medsystem.heal_attributes.recoveries.use_label", String.valueOf(consumption))).append(" - ");
        }
        Component displayName = this.displayName != CommonComponents.EMPTY ? this.displayName : type.getDisplayName();
        recoveryLabel.append(displayName).withStyle(ChatFormatting.DARK_GRAY);
        tooltipAdder.accept(recoveryLabel);
    }
}
