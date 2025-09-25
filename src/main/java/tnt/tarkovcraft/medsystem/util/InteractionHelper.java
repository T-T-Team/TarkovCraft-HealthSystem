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
            Direction direction = Direction.getApproximateNearest(vec31.x - pos.x, vec31.y - pos.y, vec31.z - pos.z);
            return BlockHitResult.miss(vec31, direction, BlockPos.containing(vec31));
        } else {
            return hitResult;
        }
    }

    public static boolean hasCooldown(LivingEntity entity, ItemStack itemStack) {
        if (entity instanceof Player player) {
            ItemCooldowns cooldowns = player.getCooldowns();
            return cooldowns.isOnCooldown(itemStack);
        }
        return false;
    }

    public static void addCooldown(LivingEntity entity, ItemStack itemStack, int cooldownTicks) {
        if (entity instanceof Player player) {
            ItemCooldowns cooldowns = player.getCooldowns();
            cooldowns.addCooldown(itemStack, cooldownTicks);
        }
    }
}
