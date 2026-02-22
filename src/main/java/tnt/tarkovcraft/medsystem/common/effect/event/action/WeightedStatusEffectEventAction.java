package tnt.tarkovcraft.medsystem.common.effect.event.action;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventActions;

public record WeightedStatusEffectEventAction(WeightedList<StatusEffectEventAction> entries) implements StatusEffectEventAction {

    public static final MapCodec<WeightedStatusEffectEventAction> CODEC = WeightedList.codec(StatusEffectEventActionType.CODEC)
            .xmap(WeightedStatusEffectEventAction::new, WeightedStatusEffectEventAction::entries).fieldOf("entries");

    @Override
    public boolean apply(StatusEffectEventContext ctx) {
        RandomSource random = ctx.getEntity().getRandom();
        return this.entries.getRandom(random)
                .map(event -> event.apply(ctx))
                .orElse(false);
    }

    @Override
    public StatusEffectEventActionType<?> getType() {
        return MedSystemStatusEffectEventActions.WEIGHTED.value();
    }
}
