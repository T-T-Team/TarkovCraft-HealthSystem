package tnt.tarkovcraft.medsystem.common.health.reaction;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemHealthReactions;

import javax.annotation.Nullable;

public class HasPainkillerHealthEventSource implements HealthEventSource {

    public static final HasPainkillerHealthEventSource INSTANCE = new HasPainkillerHealthEventSource();
    public static final MapCodec<HasPainkillerHealthEventSource> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public boolean canReact(HealthContainer container, LivingEntity entity, @Nullable DamageSource damageSource, BodyPart limb) {
        return HealthSystem.hasPainRelief(entity);
    }

    @Override
    public HealthEventSourceType<?> getType() {
        return MedSystemHealthReactions.HAS_PAINKILLER.get();
    }
}
