package tnt.tarkovcraft.medsystem.api.heal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.util.Codecs;

import java.util.Locale;
import java.util.function.Consumer;

public record HealthRecovery(int cycleDuration, float healthPerCycle, int maxCycles) implements TooltipProvider {

    public static final Codec<HealthRecovery> CODEC = RecordCodecBuilder.create(instance -> instance.group(ExtraCodecs.POSITIVE_INT.optionalFieldOf("cycleTime", 20).forGetter(HealthRecovery::cycleDuration), ExtraCodecs.POSITIVE_FLOAT.fieldOf("healAmount").forGetter(HealthRecovery::healthPerCycle), Codecs.NON_NEGATIVE_INT.optionalFieldOf("maxCycles", 1).forGetter(HealthRecovery::maxCycles)).apply(instance, HealthRecovery::new));

    public int getMaxUseDuration(int itemLimit) {
        return this.maxCycles > 0 ? this.maxCycles * this.cycleDuration : itemLimit;
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> tooltipAdder, TooltipFlag flag, DataComponentGetter componentGetter) {
        Component healthPoints = Component.literal(String.format(Locale.ROOT, "%.1f", healthPerCycle)).withStyle(ChatFormatting.GREEN);
        Component duration = Duration.format(cycleDuration).copy().withStyle(ChatFormatting.GREEN);
        if (maxCycles > 0) {
            Component healLimit = Component.literal(String.format("%.1f", healthPerCycle * maxCycles)).withStyle(ChatFormatting.YELLOW);
            tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.heal.limited", healthPoints, duration, healLimit).withStyle(ChatFormatting.GRAY));
        } else {
            tooltipAdder.accept(Component.translatable("tooltip.medsystem.heal_attributes.heal.infinite", healthPoints, duration).withStyle(ChatFormatting.GRAY));
        }
    }
}
