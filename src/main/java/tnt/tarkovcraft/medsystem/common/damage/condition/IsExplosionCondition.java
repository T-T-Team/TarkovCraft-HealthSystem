package tnt.tarkovcraft.medsystem.common.damage.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;

import java.util.Optional;

public final class IsExplosionCondition implements DamageCondition {

    private static final IsExplosionCondition INSTANCE = new IsExplosionCondition();
    public static final MapCodec<IsExplosionCondition> CODEC = MapCodec.unit(INSTANCE);

    public static Optional<Vec3> resolveExplosionPosition(HitCalculationContext ctx) {
        if (ctx.hasDamagePosition()) {
            return Optional.of(ctx.source().getSourcePosition().add(0, 0.5, 0));
        }
        Entity sourceEntity = ctx.getProjectile();
        if (sourceEntity != null) {
            return Optional.of(sourceEntity.getBoundingBox().getCenter());
        }
        return Optional.empty();
    }

    @Override
    public boolean test(HitCalculationContext hitCalculationContext) {
        DamageSource damageSource = hitCalculationContext.source();
        if (!damageSource.is(DamageTypeTags.IS_EXPLOSION)) {
            return false;
        }
        return resolveExplosionPosition(hitCalculationContext).isPresent();
    }

    @Override
    public MapCodec<? extends DamageCondition> codec() {
        return CODEC;
    }
}
