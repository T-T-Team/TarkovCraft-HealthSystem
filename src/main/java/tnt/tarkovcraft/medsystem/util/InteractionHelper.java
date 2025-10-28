package tnt.tarkovcraft.medsystem.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class InteractionHelper {

    public static HitResult filterHitResult(HitResult hitResult, Vec3 pos, double blockInteractionRange) {
        Vec3 vec3 = hitResult.getLocation();
        if (!vec3.closerThan(pos, blockInteractionRange)) {
            Vec3 vec31 = hitResult.getLocation();
            Direction direction = getApproximateNearest(vec31.x - pos.x, vec31.y - pos.y, vec31.z - pos.z);
            return BlockHitResult.miss(vec31, direction, BlockPos.containing(vec31));
        } else {
            return hitResult;
        }
    }

    public static boolean hasCooldown(LivingEntity entity, ItemStack itemStack) {
        if (entity instanceof Player player) {
            ItemCooldowns cooldowns = player.getCooldowns();
            return cooldowns.isOnCooldown(itemStack.getItem());
        }
        return false;
    }

    public static void addCooldown(LivingEntity entity, ItemStack itemStack, int cooldownTicks) {
        if (entity instanceof Player player) {
            ItemCooldowns cooldowns = player.getCooldowns();
            cooldowns.addCooldown(itemStack.getItem(), cooldownTicks);
        }
    }

    public static Direction getApproximateNearest(double x, double y, double z) {
        Direction direction = Direction.NORTH;
        double f = Float.MIN_VALUE;

        for (Direction direction1 : Direction.values()) {
            double f1 = x * direction1.getNormal().getX() + y * direction1.getNormal().getY() + z * direction1.getNormal().getZ();
            if (f1 > f) {
                f = f1;
                direction = direction1;
            }
        }

        return direction;
    }
}
