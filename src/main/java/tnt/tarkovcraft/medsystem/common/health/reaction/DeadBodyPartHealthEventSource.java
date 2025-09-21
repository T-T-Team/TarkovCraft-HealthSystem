package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;

import javax.annotation.Nullable;

public class DeadBodyPartHealthEventSource implements HealthEventSource {

    public static final DeadBodyPartHealthEventSource INSTANCE = new DeadBodyPartHealthEventSource();
    public static final MapCodec<DeadBodyPartHealthEventSource> CODEC = MapCodec.unit(INSTANCE);

    private DeadBodyPartHealthEventSource() {
    }

    @Override
    public boolean canReact(HealthContainer container, LivingEntity entity, @Nullable DamageSource damageSource, BodyPart limb) {
        return limb.isDead();
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.DEAD_BODY_PART.get();
    }
}
