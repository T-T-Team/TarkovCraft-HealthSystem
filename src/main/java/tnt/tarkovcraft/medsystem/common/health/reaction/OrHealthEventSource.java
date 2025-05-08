package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;

import java.util.List;

public class OrHealthEventSource implements HealthEventSource {

    public static final MapCodec<OrHealthEventSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HealthEventSourceType.CODEC.listOf(2, Integer.MAX_VALUE).fieldOf("values").forGetter(t -> t.reactions)
    ).apply(instance, OrHealthEventSource::new));

    private final List<HealthEventSource> reactions;

    public OrHealthEventSource(List<HealthEventSource> reactions) {
        this.reactions = reactions;
    }

    @Override
    public boolean canReact(Context context) {
        return this.reactions.stream().anyMatch(reaction -> reaction.canReact(context));
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.OR.get();
    }
}
