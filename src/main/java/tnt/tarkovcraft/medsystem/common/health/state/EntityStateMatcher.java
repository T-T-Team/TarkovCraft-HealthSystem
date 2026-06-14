package tnt.tarkovcraft.medsystem.common.health.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.init.MedSystemRegistries;

import java.util.function.Function;

public interface EntityStateMatcher {

    Codec<EntityStateMatcher> CODEC = MedSystemRegistries.STATE_MATCHER.byNameCodec()
            .dispatch(EntityStateMatcher::codec, Function.identity());

    boolean matches(LivingEntity entity);

    MapCodec<? extends EntityStateMatcher> codec();
}
