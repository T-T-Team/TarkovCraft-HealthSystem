package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventParams;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;

public record ItemPredicateEventCondition(ItemPredicate predicate) implements HealthEventCondition {

    public static final MapCodec<ItemPredicateEventCondition> CODEC = ItemPredicate.CODEC
            .xmap(ItemPredicateEventCondition::new, ItemPredicateEventCondition::predicate).fieldOf("predicate");

    @Override
    public HealthEventResult test(HealthEventContext ctx) {
        if (!ctx.hasParameter(HealthEventParams.ITEM))
            return HealthEventResult.INVALID;
        ItemStack itemStack = ctx.getParameterOrDefault(HealthEventParams.ITEM, ItemStack.EMPTY);
        return HealthEventResult.condition(this.predicate.test(itemStack));
    }

    @Override
    public MapCodec<? extends HealthEventCondition> codec() {
        return CODEC;
    }
}
