package tnt.tarkovcraft.medsystem.common.health.state;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStateFilters;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

public final class UnconsciousStateFilter implements StateFilter {

    public static final UnconsciousStateFilter INSTANCE = new UnconsciousStateFilter();
    public static final MapCodec<UnconsciousStateFilter> CODEC = MapCodec.unit(INSTANCE);

    private UnconsciousStateFilter() {
    }

    @Override
    public boolean matches(LivingEntity entity) {
        if (!BloodSystem.isEntityUnconscious(entity))
            return false;
        return entity.getVehicle() == null;
    }

    @Override
    public StateFilterType<?> getType() {
        return MedSystemStateFilters.UNCONSCIOUS.value();
    }
}
