package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;

import javax.annotation.Nullable;

public class NotHealthEventSource implements HealthEventSource {

    public static final MapCodec<NotHealthEventSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            HealthEventSourceType.CODEC.fieldOf("value").forGetter(t -> t.reaction)
    ).apply(instance, NotHealthEventSource::new));

    private final HealthEventSource reaction;

    public NotHealthEventSource(HealthEventSource reaction) {
        this.reaction = reaction;
    }

    @Override
    public boolean canReact(HealthContainer container, LivingEntity entity, @Nullable DamageSource damageSource, BodyPart limb) {
        return !this.reaction.canReact(container, entity, damageSource, limb);
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.NOT.get();
    }
}
