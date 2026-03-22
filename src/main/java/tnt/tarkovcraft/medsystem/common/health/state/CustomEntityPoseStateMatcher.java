package tnt.tarkovcraft.medsystem.common.health.state;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.common.init.CoreRegistries;
import tnt.tarkovcraft.core.common.pose.EntityPoseManager;
import tnt.tarkovcraft.core.common.pose.EntityPoseType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStateFilters;

public record CustomEntityPoseStateMatcher(Holder<EntityPoseType<?>> type) implements EntityStateMatcher {

    public static final MapCodec<CustomEntityPoseStateMatcher> CODEC = CoreRegistries.ENTITY_POSE.holderByNameCodec()
            .xmap(CustomEntityPoseStateMatcher::new, CustomEntityPoseStateMatcher::type).fieldOf("pose");

    @Override
    public boolean matches(LivingEntity entity) {
        return EntityPoseManager.isInPose(entity, this.type);
    }

    @Override
    public EntityStateMatcherType<?> getType() {
        return MedSystemStateFilters.CUSTOM_POSE.value();
    }
}
