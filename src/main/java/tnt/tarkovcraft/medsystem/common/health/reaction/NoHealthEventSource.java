package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;

public class NoHealthEventSource implements HealthEventSource {

    public static final NoHealthEventSource INSTANCE = new NoHealthEventSource();
    public static final MapCodec<NoHealthEventSource> CODEC = MapCodec.unit(INSTANCE);

    private NoHealthEventSource() {}

    @Override
    public boolean canReact(Context context) {
        return false;
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.NONE.get();
    }
}
