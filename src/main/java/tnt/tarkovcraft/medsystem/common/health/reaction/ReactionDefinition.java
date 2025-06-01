package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.core.util.context.Context;
import tnt.tarkovcraft.core.util.context.ContextKeys;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.health.reaction.event.HealthSourceEvent;
import tnt.tarkovcraft.medsystem.common.health.reaction.event.HealthSourceEventType;

import java.util.List;

public record ReactionDefinition(HealthEventSource reaction, List<HealthSourceEvent> responses) {

    public static final Codec<ReactionDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HealthEventSourceType.CODEC.fieldOf("source").forGetter(ReactionDefinition::reaction),
            Codecs.list(HealthSourceEventType.CODEC).fieldOf("events").forGetter(ReactionDefinition::responses)
    ).apply(instance, ReactionDefinition::new));

    public void react(Context context) {
        MedSystemConfig config = MedicalSystem.getConfig();
        LivingEntity entity = context.getOrThrow(ContextKeys.LIVING_ENTITY);
        boolean noEffects = entity instanceof Player player && (player.isCreative() || player.isSpectator());
        if (config.enableHitEffects && this.reaction.canReact(context) && !noEffects) {
            this.responses.forEach(resp -> resp.onReactionPassed(reaction, context));
        }
    }
}
