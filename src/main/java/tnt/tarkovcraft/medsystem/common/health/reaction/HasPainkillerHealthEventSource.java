package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;

public class HasPainkillerHealthEventSource implements HealthEventSource {

    public static final HasPainkillerHealthEventSource INSTANCE = new HasPainkillerHealthEventSource();
    public static final MapCodec<HasPainkillerHealthEventSource> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean canReact(Context context) {
        return context.get(ContextKeys.LIVING_ENTITY)
                .map(HealthSystem::hasPainRelief)
                .orElse(false);
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.HAS_PAINKILLER.get();
    }
}
