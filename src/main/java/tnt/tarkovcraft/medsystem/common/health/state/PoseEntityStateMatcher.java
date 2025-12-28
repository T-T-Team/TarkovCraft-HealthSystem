package tnt.tarkovcraft.medsystem.common.health.state;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStateFilters;

import java.util.Set;

public record PoseEntityStateMatcher(Set<Pose> targets) implements EntityStateMatcher {

    public static final MapCodec<PoseEntityStateMatcher> CODEC = Codecs.enumSet(Pose.CODEC)
            .xmap(PoseEntityStateMatcher::new, PoseEntityStateMatcher::targets).fieldOf("targets");

    @Override
    public boolean matches(LivingEntity entity) {
        Pose pose = entity.getPose();
        return this.targets.contains(pose);
    }

    @Override
    public EntityStateMatcherType<?> getType() {
        return MedSystemStateFilters.POSE.value();
    }
}
