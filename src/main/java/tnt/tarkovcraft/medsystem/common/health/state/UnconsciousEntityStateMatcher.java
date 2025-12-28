package tnt.tarkovcraft.medsystem.common.health.state;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStateFilters;
import tnt.tarkovcraft.medsystem.common.status.BloodSystem;

public final class UnconsciousEntityStateMatcher implements EntityStateMatcher {

    public static final UnconsciousEntityStateMatcher INSTANCE = new UnconsciousEntityStateMatcher();
    public static final MapCodec<UnconsciousEntityStateMatcher> CODEC = MapCodec.unit(INSTANCE);

    private UnconsciousEntityStateMatcher() {
    }

    @Override
    public boolean matches(LivingEntity entity) {
        if (!BloodSystem.isEntityUnconscious(entity))
            return false;
        return entity.getVehicle() == null;
    }

    @Override
    public EntityStateMatcherType<?> getType() {
        return MedSystemStateFilters.UNCONSCIOUS.value();
    }
}
