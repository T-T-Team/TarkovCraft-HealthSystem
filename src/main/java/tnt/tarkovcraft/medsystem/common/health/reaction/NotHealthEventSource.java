package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;

public class NotHealthEventSource implements HealthEventSource {

    public static final MapCodec<NotHealthEventSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HealthEventSourceType.CODEC.fieldOf("value").forGetter(t -> t.reaction)
    ).apply(instance, NotHealthEventSource::new));

    private final HealthEventSource reaction;

    public NotHealthEventSource(HealthEventSource reaction) {
        this.reaction = reaction;
    }

    @Override
    public boolean canReact(Context context) {
        boolean result = this.reaction.canReact(context);
        return !result;
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.NOT.get();
    }
}
