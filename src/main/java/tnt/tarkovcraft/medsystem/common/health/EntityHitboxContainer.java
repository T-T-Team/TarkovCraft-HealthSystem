package tnt.tarkovcraft.medsystem.common.health;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.calc.PositionedAABB;

import java.util.Map;

public record EntityHitboxContainer(Map<String, LimbHitboxContainer> definitions) {

    public static final Codec<EntityHitboxContainer> CODEC = Codec.unboundedMap(Codec.STRING, LimbHitboxContainer.CODEC)
            .xmap(EntityHitboxContainer::new, EntityHitboxContainer::definitions);

    public LimbHitbox getLimbHitbox(String limbCode, String state) {
        LimbHitboxContainer container = this.definitions.get(limbCode);
        return container.getByStateOrDefault(state);
    }

    public record LimbHitboxContainer(Map<String, LimbHitbox> hitboxMap) {

        public static final Codec<LimbHitboxContainer> CODEC = Codec.unboundedMap(Codec.STRING, LimbHitbox.CODEC)
                .xmap(LimbHitboxContainer::new, LimbHitboxContainer::hitboxMap).validate(container -> {
                    if (!container.hitboxMap.containsKey(HealthContainerDefinition.DEFAULT_ENTITY_STATE)) {
                        return DataResult.error(() -> "No default hitbox defined");
                    }
                    return DataResult.success(container);
                });

        public LimbHitbox getByStateOrDefault(String state) {
            return this.hitboxMap.getOrDefault(state, this.hitboxMap.get(HealthContainerDefinition.DEFAULT_ENTITY_STATE));
        }
    }

    public enum RotationMode implements StringRepresentable {

        NONE("none", TransformFunction.IDENTITY),
        PITCH_VIEW("pitch_view", RotationMode::pivotRotatePitch),
        YAW_VIEW("yaw_view", (e, aabb) -> rotateY(e.getYHeadRot(), aabb)),
        YAW_BODY("yaw_body", (e, aabb) -> rotateY(e.yBodyRot, aabb));

        public static final EnumCodec<RotationMode> CODEC = StringRepresentable.fromEnum(RotationMode::values);
        private final String serializedName;
        private final TransformFunction transform;

        RotationMode(String serializedName, TransformFunction transform) {
            this.serializedName = serializedName;
            this.transform = transform;
        }

        public PositionedAABB apply(LivingEntity entity, PositionedAABB aabb) {
            return this.transform.apply(entity, aabb);
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        private static PositionedAABB pivotRotatePitch(LivingEntity src, PositionedAABB aabb) {
            float xRot = src.getXRot();
            Vec3 pivot = aabb.center().subtract(0.0, aabb.height() / 2, 0.0);
            return aabb.pivotRotateX(-xRot * (float) Math.PI / 180.0F, pivot);
        }

        private static PositionedAABB rotateY(float rotation, PositionedAABB aabb) {
            return aabb.rotateY(-rotation * (float) Math.PI / 180.0F);
        }
    }

    public record LimbHitbox(RotationMode pitch, RotationMode yaw, PositionedAABB aabb) {

        public static final Codec<LimbHitbox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RotationMode.CODEC.optionalFieldOf("pitch", RotationMode.NONE).forGetter(LimbHitbox::pitch),
                RotationMode.CODEC.optionalFieldOf("yaw", RotationMode.NONE).forGetter(LimbHitbox::yaw),
                PositionedAABB.VEC_COMPONENT_CODEC.fieldOf("aabb").forGetter(LimbHitbox::aabb)
        ).apply(instance, LimbHitbox::new));

        public AABB toWorldSpaceHitbox(LivingEntity entity) {
            PositionedAABB aabb = this.getWithTransforms(entity);
            return aabb.move(entity.position()).aabb();
        }

        public PositionedAABB getWithTransforms(LivingEntity source) {
            return this.yaw.apply(source, this.pitch.apply(source, this.aabb));
        }

        public PositionedAABB getStatic() {
            return this.aabb;
        }
    }

    @FunctionalInterface
    public interface TransformFunction {

        TransformFunction IDENTITY = (src, aabb) -> aabb;

        PositionedAABB apply(LivingEntity entity, PositionedAABB aabb);
    }
}
