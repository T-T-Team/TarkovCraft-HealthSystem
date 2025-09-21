package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;

import javax.annotation.Nullable;

public class NoHealthEventSource implements HealthEventSource {

    public static final NoHealthEventSource INSTANCE = new NoHealthEventSource();
    public static final MapCodec<NoHealthEventSource> CODEC = MapCodec.unit(INSTANCE);

    private NoHealthEventSource() {}

    @Override
    public boolean canReact(HealthContainer container, LivingEntity entity, @Nullable DamageSource damageSource, BodyPart limb) {
        return false;
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.NONE.get();
    }
}
