package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HealthSystem;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class MeleeHitCalculator implements HitCalculator {

    public static final MeleeHitCalculator INSTANCE = new MeleeHitCalculator();

    private MeleeHitCalculator() {
    }

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        List<HitResult> hits = new ArrayList<>();
        Entity attacker = source.getEntity();
        float reachRange = 6.0F;
        Vec3 from = attacker.getType() == EntityType.PLAYER ? attacker.getEyePosition() : new Vec3(attacker.getX(), attacker.getY() + attacker.getBbHeight() / 2.0, attacker.getZ());
        Vec3 to = from.add(attacker.getLookAngle().scale(reachRange));
        // Try to find directly hit limb
        container.iterateHitboxes(
                entity,
                (hitbox, limb) -> {
                    AABB aabb = hitbox.toWorldSpaceHitbox(entity);
                    PositionedAABB.tryIntersect(aabb, from, to).ifPresent(hit -> hits.add(new HitResult(hitbox, limb, aabb, hit)));
                }
        );
        if (!hits.isEmpty()) {
            hits.sort(Comparator.comparingDouble(res -> res.aabb().distanceToSqr(from)));
            return Collections.singletonList(hits.getFirst());
        }

        // No hitboxes were hit, get closest most likely hit limb if the entity type allows hit approximation
        List<HitResult> result = null;
        if (!attacker.getType().is(MedSystemTags.Entities.NO_LIMB_HIT_APPROXIMATION)) {
            result = HealthSystem.getClosestPossibleHits(
                    attacker.getBoundingBox().getCenter(),
                    entity,
                    container,
                    (hitbox, part) -> !part.isDead()
            );
        }

        return result == null || result.isEmpty() ? Collections.emptyList() : Collections.singletonList(result.getFirst());
    }
}
