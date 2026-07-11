package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import tnt.tarkovcraft.medsystem.common.health.distributor.ScaledDamageDistributor;
import tnt.tarkovcraft.medsystem.util.HitboxHelper;

public record InLiquidDamageCalculator(float damageScale) implements HitCalculator {

    @Override
    public HitCalculationResult calculateHits(HitCalculationContext context) {
        HitCalculationResult result = HitCalculationResult.simpleResult(context, hitbox -> isInFluid(context.entity(), hitbox));
        if (result.isMiss()) {
            // nothing is apparently in fluid, add leg hitboxes
            result = HitCalculationResult.simpleResult(context, hitbox -> hitbox.limb().isLeg());
        }
        return result.withDamageDistributor(original -> new ScaledDamageDistributor(this.damageScale, original));
    }

    private boolean isInFluid(LivingEntity entity, LimbHitbox hitbox) {
        AABB aabb = hitbox.worldspaceAABB(entity);
        Boolean result = HitboxHelper.tracePoint(true, aabb, point -> {
            BlockPos pos = BlockPos.containing(point);
            FluidState fluidState = entity.level().getFluidState(pos);
            if (!fluidState.isEmpty()) {
                return true;
            }
            return null;
        });
        return Boolean.TRUE.equals(result);
    }
}
