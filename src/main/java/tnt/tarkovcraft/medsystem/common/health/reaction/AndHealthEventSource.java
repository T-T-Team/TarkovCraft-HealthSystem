package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;

import javax.annotation.Nullable;
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
    public boolean canReact(HealthContainer container, LivingEntity entity, @Nullable DamageSource damageSource, Limb limb) {
        return this.reactions.stream().allMatch(react -> react.canReact(container, entity, damageSource, limb));
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.AND.get();
    }
}
