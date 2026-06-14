package tnt.tarkovcraft.medsystem.common.health.state;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class SittingPassengerEntityStateMatcher implements EntityStateMatcher {

    public static final SittingPassengerEntityStateMatcher INSTANCE = new SittingPassengerEntityStateMatcher();
    public static final MapCodec<SittingPassengerEntityStateMatcher> CODEC = MapCodec.unit(INSTANCE);

    private SittingPassengerEntityStateMatcher() {
    }

    @Override
    public boolean matches(LivingEntity entity) {
        Entity vehicle = entity.getVehicle();
        return vehicle != null && vehicle.shouldRiderSit();
    }

    @Override
    public MapCodec<? extends EntityStateMatcher> codec() {
        return CODEC;
    }
}
