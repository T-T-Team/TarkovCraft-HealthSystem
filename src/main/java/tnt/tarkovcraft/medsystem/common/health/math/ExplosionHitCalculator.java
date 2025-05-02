package tnt.tarkovcraft.medsystem.common.health.math;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.config.MedSystemConfig;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.HitResult;

import java.util.ArrayList;
import java.util.List;

public class ExplosionHitCalculator implements HitCalculator {

    public static final ExplosionHitCalculator INSTANCE = new ExplosionHitCalculator();

    public static boolean isValidExplosionSource(DamageSource source) {
        return source.is(DamageTypeTags.IS_EXPLOSION) && source.getSourcePosition() != null;
    }

    @Override
    public List<HitResult> calculateHits(LivingEntity entity, DamageSource source, HealthContainer container) {
        Vec3 explosionPosition = source.getSourcePosition();
        List<HitResult> hits = new ArrayList<>();
        container.acceptHitboxes(
                (hitbox, part) -> isVisible(hitbox.getLevelPositionedAABB(entity), explosionPosition, entity.level(), entity),
                (hitbox, part) -> hits.add(new HitResult(hitbox, part))
        );
        return hits;
    }

    @Override
    public DamageDistributor getCustomDamageDistributor(LivingEntity entity, DamageSource source, HealthContainer container, DamageDistributor original) {
        float scale = MedicalSystem.getConfig().explosionDamageScale;
        return new ScaledDamageDistributor(scale, FullDamageDistributor.INSTANCE);
    }

    protected boolean isVisible(AABB hitbox, Vec3 position, Level level, LivingEntity entity) {
        Vec3 hitboxPosition = hitbox.getCenter();
        ClipContext context = new ClipContext(position, hitboxPosition, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity);
        BlockHitResult result = level.clip(context);
        return result.getType() == net.minecraft.world.phys.HitResult.Type.MISS;
    }
}
