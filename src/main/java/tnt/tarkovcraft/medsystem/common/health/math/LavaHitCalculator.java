package tnt.tarkovcraft.medsystem.common.health.math;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.BodyPartGroup;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HitResult;

import java.util.ArrayList;
import java.util.List;

public class LavaHitCalculator implements HitCalculator {

    public static final LavaHitCalculator INSTANCE = new LavaHitCalculator();

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        List<HitResult> hits = new ArrayList<>();
        container.acceptHitboxes(
                (hitbox, part) -> isInFluid(entity.level(), hitbox.getLevelPositionedAABB(entity)),
                (hitbox, bodyPart) -> hits.add(new HitResult(hitbox, bodyPart))
        );
        if (hits.isEmpty()) {
            // nothing is apparently in fluid, add leg hitboxes
            container.acceptHitboxes(
                    (hitbox, part) -> part.getGroup() == BodyPartGroup.LEG,
                    (hitbox, part) -> hits.add(new HitResult(hitbox, part))
            );
        }
        return hits;
    }

    protected boolean isInFluid(Level level, AABB aabb) {
        Vec3 pos = aabb.getCenter();
        FluidState state = level.getFluidState(new BlockPos(Mth.floor(pos.x), Mth.floor(pos.y), Mth.floor(pos.z)));
        return !state.isEmpty();
    }
}
