package tnt.tarkovcraft.medsystem.api;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class LimbDamageSource extends DamageSource implements SpecificLimbDamage {

    private final String[] limbs;
    private boolean damageDeadLimbs = true;

    public LimbDamageSource(Holder<DamageType> type, @Nullable Entity directEntity, @Nullable Entity causingEntity, @Nullable Vec3 damageSourcePosition, String... limbs) {
        super(type, directEntity, causingEntity, damageSourcePosition);
        this.limbs = limbs;
    }

    public LimbDamageSource(Holder<DamageType> type, @Nullable Entity directEntity, @Nullable Entity causingEntity, String... limbs) {
        super(type, directEntity, causingEntity);
        this.limbs = limbs;
    }

    public LimbDamageSource(Holder<DamageType> type, Vec3 damageSourcePosition, String... limbs) {
        super(type, damageSourcePosition);
        this.limbs = limbs;
    }

    public LimbDamageSource(Holder<DamageType> type, @Nullable Entity entity, String... limbs) {
        super(type, entity);
        this.limbs = limbs;
    }

    public LimbDamageSource(Holder<DamageType> type, String... limbs) {
        super(type);
        this.limbs = limbs;
    }

    public void setDamageDeadLimbs(boolean damageDeadLimbs) {
        this.damageDeadLimbs = damageDeadLimbs;
    }

    @Override
    public String[] getLimbs() {
        return limbs;
    }

    @Override
    public boolean canDamageDeadLimbs() {
        return this.damageDeadLimbs;
    }
}
