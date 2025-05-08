package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;

import java.util.List;

public class AndHealthEventSource implements HealthEventSource {

    public static final MapCodec<AndHealthEventSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HealthEventSourceType.CODEC.listOf(2, Integer.MAX_VALUE).fieldOf("values").forGetter(t -> t.reactions)
    ).apply(instance, AndHealthEventSource::new));

    private final List<HealthEventSource> reactions;

    public AndHealthEventSource(List<HealthEventSource> reactions) {
        this.reactions = reactions;
    }

    @Override
    public boolean canReact(Context context) {
        return this.reactions.stream().allMatch(react -> react.canReact(context));
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.AND.get();
    }
}
