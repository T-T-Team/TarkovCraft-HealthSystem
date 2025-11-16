package tnt.tarkovcraft.medsystem.common.armor;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public record ArmorMaterial(Component title, float deflectionChance) implements TooltipProvider {

    public static final Codec<ArmorMaterial> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentSerialization.CODEC.optionalFieldOf("title", CommonComponents.EMPTY).forGetter(ArmorMaterial::title),
            Codec.FLOAT.fieldOf("deflectionChance").forGetter(ArmorMaterial::deflectionChance)
    ).apply(instance, ArmorMaterial::new));

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        if (this.title != CommonComponents.EMPTY) {
            consumer.accept(Component.translatable("tooltip.medsystem.armor.material", this.title));
        }
    }
}
