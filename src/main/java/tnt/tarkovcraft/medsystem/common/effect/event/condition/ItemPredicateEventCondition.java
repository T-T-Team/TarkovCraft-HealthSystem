package tnt.tarkovcraft.medsystem.common.effect.event.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventParams;
import tnt.tarkovcraft.medsystem.common.effect.event.TriggerResult;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventConditions;

public record ItemPredicateEventCondition(ItemPredicate predicate) implements StatusEffectEventCondition {

    public static final MapCodec<ItemPredicateEventCondition> CODEC = ItemPredicate.CODEC
            .xmap(ItemPredicateEventCondition::new, ItemPredicateEventCondition::predicate).fieldOf("predicate");

    @Override
    public TriggerResult test(StatusEffectEventContext ctx) {
        if (!ctx.hasParameter(StatusEffectEventParams.ITEM))
            return TriggerResult.INVALID;
        ItemStack itemStack = ctx.getParameterOrDefault(StatusEffectEventParams.ITEM, ItemStack.EMPTY);
        return TriggerResult.condition(this.predicate.test(itemStack));
    }

    @Override
    public StatusEffectEventConditionType<?> getType() {
        return MedSystemStatusEffectEventConditions.ITEM_PREDICATE.value();
    }
}
