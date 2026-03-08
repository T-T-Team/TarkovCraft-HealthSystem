package tnt.tarkovcraft.medsystem.common.health.state;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodSystemManager;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStateFilters;

public final class UnconsciousEntityStateMatcher implements EntityStateMatcher {

    public static final UnconsciousEntityStateMatcher INSTANCE = new UnconsciousEntityStateMatcher();
    public static final MapCodec<UnconsciousEntityStateMatcher> CODEC = MapCodec.unit(INSTANCE);

    private UnconsciousEntityStateMatcher() {
    }

    @Override
    public boolean matches(LivingEntity entity) {
        if (!BloodSystemManager.isUnconscious(entity))
            return false;
        return entity.getVehicle() == null;
    }

    @Override
    public EntityStateMatcherType<?> getType() {
        return MedSystemStateFilters.UNCONSCIOUS.value();
    }
}
