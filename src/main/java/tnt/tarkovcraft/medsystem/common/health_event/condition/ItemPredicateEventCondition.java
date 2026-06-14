package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventParams;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;

public record ItemPredicateEventCondition(TagKey<Item> predicate) implements HealthEventCondition {

    public static final MapCodec<ItemPredicateEventCondition> CODEC = TagKey.codec(Registries.ITEM)
            .xmap(ItemPredicateEventCondition::new, ItemPredicateEventCondition::predicate).fieldOf("predicate");

    @Override
    public HealthEventResult test(HealthEventContext ctx) {
        if (!ctx.hasParameter(HealthEventParams.ITEM))
            return HealthEventResult.INVALID;
        ItemStack itemStack = ctx.getParameterOrDefault(HealthEventParams.ITEM, ItemStack.EMPTY);
        return HealthEventResult.condition(itemStack.is(this.predicate));
    }

    @Override
    public MapCodec<? extends HealthEventCondition> codec() {
        return CODEC;
    }
}
