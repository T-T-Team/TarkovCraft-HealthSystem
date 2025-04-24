package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.Codec;
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
            Codecs.list(Codecs.simpleEnumCodec(Pose.class)).fieldOf("allow").forGetter(t -> new ArrayList<>(t.allows)),
            Codec.BOOL.optionalFieldOf("invert", false).forGetter(t -> t.invert)
    ).apply(instance, EntityPoseTransformCondition::new));

    private final Set<Pose> allows;
    private final boolean invert;

    public EntityPoseTransformCondition(List<Pose> allows, boolean invert) {
        this.allows = EnumSet.copyOf(allows);
        this.invert = invert;
    }

    @Override
    public boolean canApply(LivingEntity context) {
        boolean contains = this.allows.contains(context.getPose());
        return contains != this.invert;
    }

    @Override
    public TransformConditionType<?> getType() {
        return MedSystemTransformConditions.ENTITY_POSE.get();
    }
}
