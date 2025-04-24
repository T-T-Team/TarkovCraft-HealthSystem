package tnt.tarkovcraft.medsystem.common.health.transform;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.health.PositionedAABB;

import java.util.Collections;
import java.util.List;

public record HitboxTransformDefinition(List<TransformCondition> conditions, List<EntityHitboxTransform> transforms) {

    public static final Codec<HitboxTransformDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.list(TransformConditionType.CODEC).optionalFieldOf("if",Collections.emptyList()).forGetter(HitboxTransformDefinition::conditions),
            Codecs.list(EntityHitboxTransformType.CODEC).fieldOf("apply").forGetter(HitboxTransformDefinition::transforms)
    ).apply(instance, HitboxTransformDefinition::new));

    public boolean canApplyTransform(LivingEntity entity) {
        for (TransformCondition condition : this.conditions) {
            if (!condition.canApply(entity))
                return false;
        }
        return true;
    }

    public PositionedAABB apply(PositionedAABB aabb, LivingEntity entity) {
        if (this.canApplyTransform(entity)) {
            for (EntityHitboxTransform transform : this.transforms) {
                aabb = transform.apply(aabb, entity);
            }
        }
        return aabb;
    }
}
