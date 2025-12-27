package tnt.tarkovcraft.medsystem.common.health.state;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStateFilters;

public final class SittingPassengerStateFilter implements StateFilter {

    public static final SittingPassengerStateFilter INSTANCE = new SittingPassengerStateFilter();
    public static final MapCodec<SittingPassengerStateFilter> CODEC = MapCodec.unit(INSTANCE);

    private SittingPassengerStateFilter() {
    }

    @Override
    public boolean matches(LivingEntity entity) {
        Entity vehicle = entity.getVehicle();
        return vehicle != null && vehicle.shouldRiderSit();
    }

    @Override
    public StateFilterType<?> getType() {
        return MedSystemStateFilters.SITTING_PASSENGER.value();
    }
}
