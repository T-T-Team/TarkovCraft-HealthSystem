package tnt.tarkovcraft.medsystem.common.health.state;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStateFilters;

public final class IsBabyStateFilter implements StateFilter {

    public static final IsBabyStateFilter INSTANCE = new IsBabyStateFilter();
    public static final MapCodec<IsBabyStateFilter> CODEC = MapCodec.unit(INSTANCE);

    private IsBabyStateFilter() {
    }

    @Override
    public boolean matches(LivingEntity entity) {
        return entity.isBaby();
    }

    @Override
    public StateFilterType<?> getType() {
        return MedSystemStateFilters.IS_BABY.value();
    }
}
