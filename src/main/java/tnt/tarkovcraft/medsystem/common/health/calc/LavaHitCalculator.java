package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.distributor.ScaledDamageDistributor;

public final class LavaHitCalculator implements HitCalculator {

    public static final LavaHitCalculator INSTANCE = new LavaHitCalculator();

    private LavaHitCalculator() {
    }

    public static boolean canApply(HitCalculationContext context) {
        return context.source() == context.entity().damageSources().lava();
    }

    @Override
    public HitCalculationResult calculateHits(HitCalculationContext context) {
        HitCalculationResult result = HitCalculationResult.simpleResult(context, hitbox -> isInFluid(context.entity(), hitbox));
        if (result.isMiss()) {
            // nothing is apparently in fluid, add leg hitboxes
            result = HitCalculationResult.simpleResult(context, hitbox -> hitbox.limb().isLeg());
        }
        return result.withDamageDistributor(original -> new ScaledDamageDistributor(2.5F, original));
    }

    private boolean isInFluid(LivingEntity entity, LimbHitbox hitbox) {
        AABB aabb = hitbox.worldspaceAABB(entity);
        Vec3 pos = aabb.getCenter();
        FluidState state = entity.level().getFluidState(new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z)));
        return !state.isEmpty();
    }
}
