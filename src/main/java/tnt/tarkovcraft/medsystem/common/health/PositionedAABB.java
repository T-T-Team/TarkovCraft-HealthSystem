package tnt.tarkovcraft.medsystem.common.health;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.transform.EntityHitboxTransform;

import java.util.Optional;

public final class PositionedAABB {

    private final Vec3 vec3;
    private final AABB aabb;

    private PositionedAABB(Vec3 vec3, double width, double height) {
        this.vec3 = vec3;
        this.aabb = new AABB(
                vec3.x - width, vec3.y - height, vec3.z - width,
                vec3.x + width, vec3.y + height, vec3.z + width
        );
    }

    public static PositionedAABB create(Vec3 pos, Vec2 size) {
        return new PositionedAABB(pos, size.x, size.y);
    }

    public static PositionedAABB create(double x, double y, double z, double width, double height) {
        return create(new Vec3(x, y, z), width, height);
    }

    public static PositionedAABB create(Vec3 vec3, double width, double height) {
        return new PositionedAABB(vec3, width, height);
    }

    public PositionedAABB resize(double width, double height) {
        return new PositionedAABB(vec3, width, height);
    }

    public PositionedAABB resize(double size) {
        return this.resize(size, size);
    }

    public PositionedAABB resize(Vec2 size) {
        return this.resize(size.x, size.y);
    }

    public PositionedAABB move(double x, double y, double z) {
        double xs = aabb.getXsize() / 2.0;
        double ys = aabb.getYsize() / 2.0;
        return new PositionedAABB(vec3.add(x, y, z), xs, ys);
    }

    public PositionedAABB move(Vec3 offset) {
        return this.move(offset.x, offset.y, offset.z);
    }

    public PositionedAABB rotateX(float rotationX) {
        double xs = aabb.getXsize() / 2.0;
        double ys = aabb.getYsize() / 2.0;
        return new PositionedAABB(vec3.xRot(rotationX), xs, ys);
    }

    public PositionedAABB pivotRotateX(float rotationX, Vec3 pivot) {
        double xs = aabb.getXsize() / 2.0;
        double ys = aabb.getYsize() / 2.0;
        Vec3 adjusted = this.vec3.subtract(pivot).xRot(rotationX).add(pivot);
        return new PositionedAABB(adjusted, xs, ys);
    }

    public PositionedAABB rotateY(float rotationY) {
        double xs = aabb.getXsize() / 2.0;
        double ys = aabb.getYsize() / 2.0;
        return new PositionedAABB(vec3.yRot(rotationY), xs, ys);
    }

    public PositionedAABB scale(double scaleX, double scaleY) {
        double xs = aabb.getXsize() / 2.0 * scaleX;
        double ys = aabb.getYsize() / 2.0 * scaleY;
        return new PositionedAABB(vec3, xs, ys);
    }

    public PositionedAABB scale(double scale) {
        return this.scale(scale, scale);
    }

    public Optional<Vec3> intersect(Vec3 pointFrom, Vec3 pointTo) {
        return tryIntersect(this.aabb, pointFrom, pointTo);
    }

    public Vec3 center() {
        return this.vec3;
    }

    public Vec2 size() {
        return new Vec2((float) aabb.getXsize(), (float) aabb.getYsize());
    }

    public double height() {
        return aabb.getYsize();
    }

    public AABB aabb() {
        return this.aabb;
    }

    public PositionedAABB transform(EntityHitboxTransform transform, LivingEntity context) {
        return transform.apply(this, context);
    }

    public static Optional<Vec3> tryIntersect(AABB aabb, Vec3 start, Vec3 end) {
        Optional<Vec3> optional = aabb.clip(start, end);
        if (aabb.contains(start)) {
            return Optional.of(start);
        } else if (aabb.contains(end)) {
            return Optional.of(end);
        } else {
            return optional;
        }
    }
}
