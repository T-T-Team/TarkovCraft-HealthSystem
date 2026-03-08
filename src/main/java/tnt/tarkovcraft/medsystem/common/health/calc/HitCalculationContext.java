package tnt.tarkovcraft.medsystem.common.health.calc;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.init.MedSystemTags;
import tnt.tarkovcraft.medsystem.util.HitboxHelper;

public record HitCalculationContext(LivingEntity entity, HealthContainer container, DamageSource source) {

    public @Nullable Entity getAttackingEntity() {
        return this.source.getEntity();
    }

    public @Nullable Entity getProjectile() {
        return !this.source.isDirect() ? this.source.getDirectEntity() : null;
    }

    public boolean isDamageType(TagKey<DamageType> tag) {
        return this.source.is(tag);
    }

    public boolean isDamage(ResourceKey<DamageType> key) {
        return this.source.is(key);
    }

    public boolean hasDamagePosition() {
        Vec3 sourcePosition = this.source.getSourcePosition();
        return sourcePosition != null && !Vec3.ZERO.equals(sourcePosition);
    }

    public boolean allowHitApproximation(boolean fallback) {
        Entity attacker = this.getAttackingEntity();
        if (attacker == null) {
            return fallback;
        }
        EntityType<?> type = attacker.getType();
        return !type.is(MedSystemTags.Entities.NO_LIMB_HIT_APPROXIMATION);
    }

    public boolean allowHitApproximation() {
        return this.allowHitApproximation(false);
    }

    public HitCalculationResult approximate(Ray ray) {
        if (!this.allowHitApproximation()) {
            return HitCalculationResult.miss(ray);
        }
        HitInfo info = HitboxHelper.approximateHits(ray, this.entity, this.container)
                .findFirst()
                .orElse(null);
        return HitCalculationResult.of(info)
                .withRayCast(ray);
    }
}
