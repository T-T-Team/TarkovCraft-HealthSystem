package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.MedicalSystemContextKeys;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;

public class DeadBodyPartHealthEventSource implements HealthEventSource {

    public static final DeadBodyPartHealthEventSource INSTANCE = new DeadBodyPartHealthEventSource();
    public static final MapCodec<DeadBodyPartHealthEventSource> CODEC = MapCodec.unit(INSTANCE);

    private DeadBodyPartHealthEventSource() {
    }

    @Override
    public boolean canReact(Context context) {
        return context.get(MedicalSystemContextKeys.BODY_PART)
                .map(BodyPart::isDead)
                .orElse(false);
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.DEAD_BODY_PART.get();
    }
}
