package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.health.EntityHitboxContainer;
import tnt.tarkovcraft.medsystem.common.health.distributor.DamageDistributor;
import tnt.tarkovcraft.medsystem.common.health.distributor.ScaledDamageDistributor;

import java.util.ArrayList;
import java.util.List;

public final class LavaHitCalculator implements HitCalculator {

    public static final LavaHitCalculator INSTANCE = new LavaHitCalculator();

    private LavaHitCalculator() {
    }

    public static boolean canApply(HitCalculatorRule.Context context) {
        return context.source() == context.target().damageSources().lava();
    }

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        List<HitResult> hits = new ArrayList<>();
        container.iterateHitboxes(
                entity,
                (hitbox, part) -> isInFluid(entity, hitbox),
                (hitbox, bodyPart) -> hits.add(new HitResult(hitbox, bodyPart))
        );
        if (hits.isEmpty()) {
            // nothing is apparently in fluid, add leg hitboxes
            container.iterateHitboxes(
                    entity,
                    (hitbox, part) -> part.getType() == LimbType.LEG,
                    (hitbox, part) -> hits.add(new HitResult(hitbox, part))
            );
        }
        return hits;
    }

    @Override
    public DamageDistributor getCustomDamageDistributor(LivingEntity entity, DamageSource source, HealthContainer container, DamageDistributor original) {
        return new ScaledDamageDistributor(2.5F, original);
    }

    protected boolean isInFluid(LivingEntity entity, EntityHitboxContainer.LimbHitbox hitbox) {
        AABB aabb = hitbox.toWorldSpaceHitbox(entity);
        Vec3 pos = aabb.getCenter();
        FluidState state = entity.level().getFluidState(new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z)));
        return !state.isEmpty();
    }
}
