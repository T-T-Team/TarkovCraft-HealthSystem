package tnt.tarkovcraft.medsystem.common.health_event.action;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.util.WeightedList;

public record WeightedEventAction(WeightedList<HealthEventAction> entries) implements HealthEventAction {

    public static final MapCodec<WeightedEventAction> CODEC = WeightedList.codec(HealthEventAction.CODEC)
            .xmap(WeightedEventAction::new, WeightedEventAction::entries).fieldOf("entries");

    @Override
    public boolean apply(HealthEventContext ctx) {
        RandomSource random = ctx.getEntity().getRandom();
        return this.entries.getRandom(random)
                .map(event -> event.apply(ctx))
                .orElse(false);
    }

    @Override
    public MapCodec<? extends HealthEventAction> codec() {
        return CODEC;
    }
}
