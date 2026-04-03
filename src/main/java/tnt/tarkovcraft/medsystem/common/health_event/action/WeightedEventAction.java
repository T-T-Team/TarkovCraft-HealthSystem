package tnt.tarkovcraft.medsystem.common.health_event.action;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthEventActions;

public record WeightedEventAction(WeightedList<HealthEventAction> entries) implements HealthEventAction {

    public static final MapCodec<WeightedEventAction> CODEC = WeightedList.codec(HealthEventActionType.CODEC)
            .xmap(WeightedEventAction::new, WeightedEventAction::entries).fieldOf("entries");

    @Override
    public boolean apply(HealthEventContext ctx) {
        RandomSource random = ctx.getEntity().getRandom();
        return this.entries.getRandom(random)
                .map(event -> event.apply(ctx))
                .orElse(false);
    }

    @Override
    public HealthEventActionType<?> getType() {
        return MedSystemHealthEventActions.WEIGHTED.value();
    }
}
