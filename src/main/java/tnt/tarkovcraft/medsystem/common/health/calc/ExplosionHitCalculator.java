package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.EntityHitboxContainer;
import tnt.tarkovcraft.medsystem.common.health.distributor.DamageDistributor;
import tnt.tarkovcraft.medsystem.common.health.distributor.FullDamageDistributor;
import tnt.tarkovcraft.medsystem.common.health.distributor.ScaledDamageDistributor;

import java.util.ArrayList;
import java.util.List;

public class ExplosionHitCalculator implements HitCalculator {

    public static final ExplosionHitCalculator INSTANCE = new ExplosionHitCalculator();

    public static boolean canApply(HitCalculatorRule.Context ctx) {
        DamageSource source = ctx.source();
        Vec3 sourcePosition = source.getSourcePosition();
        return source.is(DamageTypeTags.IS_EXPLOSION) && !Vec3.ZERO.equals(sourcePosition);
    }

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        Vec3 explosionPosition = source.getSourcePosition();
        List<HitResult> hits = new ArrayList<>();
        container.iterateHitboxes(
                entity,
                (hitbox, part) -> isVisible(hitbox, explosionPosition, entity.level(), entity),
                (hitbox, part) -> hits.add(new HitResult(hitbox, part))
        );
        return hits;
    }

    @Override
    public DamageDistributor getCustomDamageDistributor(LivingEntity entity, DamageSource source, HealthContainer container, DamageDistributor original) {
        float scale = MedicalSystem.getConfig().explosionDamageScale;
        return new ScaledDamageDistributor(scale, FullDamageDistributor.INSTANCE);
    }

    protected boolean isVisible(EntityHitboxContainer.LimbHitbox hitbox, Vec3 position, Level level, LivingEntity entity) {
        AABB aabb = hitbox.toWorldSpaceHitbox(entity);
        Vec3 hitboxPosition = aabb.getCenter();
        ClipContext context = new ClipContext(position, hitboxPosition, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
        BlockHitResult result = level.clip(context);
        return result.getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }
}
