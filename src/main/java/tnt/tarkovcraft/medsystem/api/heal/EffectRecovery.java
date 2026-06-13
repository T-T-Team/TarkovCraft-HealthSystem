package tnt.tarkovcraft.medsystem.api.heal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.LimbContainer;

import java.util.function.Consumer;

public record EffectRecovery(int consumption, EffectRecoveryApplicator applicator, boolean extendedTooltip) implements TooltipProvider {

    public static final Codec<EffectRecovery> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("consumption", 1).forGetter(EffectRecovery::consumption),
            EffectRecoveryApplicator.CODEC.fieldOf("applicator").forGetter(EffectRecovery::applicator),
            Codec.BOOL.optionalFieldOf("extended_tooltip", true).forGetter(EffectRecovery::extendedTooltip)
    ).apply(instance, EffectRecovery::new));

    public boolean canRecover(HealthContainer container, LivingEntity entity, @Nullable Limb limb) {
        return limb != null
                ? this.applicator.findRecoverableEffect(container, entity, limb).isPresent()
                : this.canUse(container, entity);
    }

    public boolean canUse(HealthContainer container, LivingEntity entity) {
        LimbContainer limbContainer = container.getLimbContainer();
        return limbContainer.getLimbs()
                .anyMatch(limb -> this.applicator.findRecoverableEffect(container, entity, limb).isPresent());
    }

    public void recover(HealthContainer container, LivingEntity entity, Limb limb) {
        this.applicator.findRecoverableEffect(container, entity, limb).ifPresent(effect -> {
            this.applicator.apply(container, entity, effect, limb);
        });
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter) {
        Component template = Component.literal("> ").withStyle(ChatFormatting.DARK_GRAY);
        this.applicator.addLabels(text -> {
            MutableComponent label = template.copy();
            label.append(text.plainCopy());
            if (this.extendedTooltip && this.consumption > 1) {
                Component usesLabel = Component.translatable("tooltip.medsystem.heal_attributes.recoveries.use_label", String.valueOf(consumption)).withStyle(ChatFormatting.DARK_GRAY);
                label.append(" (+").append(usesLabel).append(")");
            }
            tooltipAdder.accept(label);
        });
    }
}
