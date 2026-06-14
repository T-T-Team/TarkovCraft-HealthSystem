package tnt.tarkovcraft.medsystem.common.health.state;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;

public final class IsBabyEntityStateMatcher implements EntityStateMatcher {

    public static final IsBabyEntityStateMatcher INSTANCE = new IsBabyEntityStateMatcher();
    public static final MapCodec<IsBabyEntityStateMatcher> CODEC = MapCodec.unit(INSTANCE);

    private IsBabyEntityStateMatcher() {
    }

    @Override
    public boolean matches(LivingEntity entity) {
        return entity.isBaby();
    }

    @Override
    public MapCodec<? extends EntityStateMatcher> codec() {
        return CODEC;
    }
}
