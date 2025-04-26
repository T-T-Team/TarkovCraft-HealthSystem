package tnt.tarkovcraft.medsystem.common.health.math;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ProjectileHitCalculator implements HitCalculator {

    public static final ProjectileHitCalculator INSTANCE = new ProjectileHitCalculator();

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        Entity projectile = source.getDirectEntity();
        Vec3 position = projectile.getBoundingBox().getCenter();
        Vec3 destPosition = position.add(projectile.getDeltaMovement().scale(2.5D));
        int pierceAmount = HealthSystem.getProjectilePiercing(entity, source, container, projectile);
        List<BodyPartHitbox> hitboxes = container.getDefinition().getHitboxes();
        List<HitResult> hits = new ArrayList<>();
        for (BodyPartHitbox hitbox : hitboxes) {
            AABB axisAlignedBB = hitbox.getLevelPositionedAABB(entity).inflate(0.3F);
            Optional<Vec3> intersect = PositionedAABB.tryIntersect(axisAlignedBB, position, destPosition);
            intersect.ifPresent(hit -> hits.add(new HitResult(hitbox, container.getBodyPart(hitbox.getOwner()), axisAlignedBB, hit)));
        }
        hits.sort(Comparator.comparingDouble(res -> res.aabb().distanceToSqr(position)));
        return hits.subList(0, Math.min(pierceAmount, hits.size()));
    }
}
