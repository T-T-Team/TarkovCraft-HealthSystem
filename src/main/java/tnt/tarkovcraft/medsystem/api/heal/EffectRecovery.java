package tnt.tarkovcraft.medsystem.api.heal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextImpl;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.common.MedicalSystemContextKeys;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectMap;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffectType;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public record EffectRecovery(int consumption, Holder<StatusEffectType<?>> effect, boolean extendedTooltip) implements TooltipProvider {

    public static final Codec<EffectRecovery> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("consumption", 1).forGetter(EffectRecovery::consumption),
            MedSystemRegistries.STATUS_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(EffectRecovery::effect),
            Codec.BOOL.optionalFieldOf("extendedTooltip", true).forGetter(EffectRecovery::extendedTooltip)
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
        Context context = ContextImpl.of(ContextKeys.LIVING_ENTITY, entity, MedicalSystemContextKeys.HEALTH_CONTAINER, container, LootContextParams.TOOL, stack);
        StatusEffect statusEffect = effects.remove(this.effect.value(), context);
        if (statusEffect != null) {
            effects.addEffect(statusEffect);
        }
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter) {
        StatusEffectType<?> type = effect.value();
        MutableComponent recoveryLabel = Component.literal("- ");
        if (this.extendedTooltip) {
            recoveryLabel.append(Component.translatable("tooltip.medsystem.heal_attributes.recoveries.use_label", String.valueOf(consumption))).append(" - ");
        }
        recoveryLabel.append(type.getDisplayName()).withStyle(ChatFormatting.DARK_GRAY);
        tooltipAdder.accept(recoveryLabel);
    }
}
