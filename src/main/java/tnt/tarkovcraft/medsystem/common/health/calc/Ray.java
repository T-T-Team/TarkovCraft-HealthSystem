package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.world.phys.Vec3;

public record Ray(Vec3 from, Vec3 to) {

    public static Ray create(Vec3 from, Vec3 to) {
        return new Ray(from, to);
    }

    public Vec3 direction() {
        return this.from.subtract(this.to);
    }

    public Vec3 startDirectionTo(Vec3 point) {
        return this.from.subtract(point);
    }

    public Vec3 endDirectionTo(Vec3 point) {
        return this.to.subtract(point);
    }
}
