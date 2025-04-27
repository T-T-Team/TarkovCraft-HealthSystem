package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTransformConditions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class EntityPoseTransformCondition implements TransformCondition {

    public static final MapCodec<EntityPoseTransformCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codecs.list(Codecs.simpleEnumCodec(Pose.class)).fieldOf("allow").forGetter(t -> new ArrayList<>(t.allows))
    ).apply(instance, EntityPoseTransformCondition::new));

    private final Set<Pose> allows;

    public EntityPoseTransformCondition(List<Pose> allows) {
        this.allows = EnumSet.copyOf(allows);
    }

    @Override
    public boolean canApply(LivingEntity context) {
        return this.allows.contains(context.getPose());
    }

    @Override
    public TransformConditionType<?> getType() {
        return MedSystemTransformConditions.ENTITY_POSE.get();
    }
}
