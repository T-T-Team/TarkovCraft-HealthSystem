package tnt.tarkovcraft.medsystem.api;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class LimbDamageSource extends DamageSource implements SpecificLimbDamage {

    private final String[] bodyParts;
    private boolean allowDeadBodyPartDamage = true;

    public LimbDamageSource(Holder<DamageType> type, @Nullable Entity directEntity, @Nullable Entity causingEntity, @Nullable Vec3 damageSourcePosition, String... bodyParts) {
        super(type, directEntity, causingEntity, damageSourcePosition);
        this.bodyParts = bodyParts;
    }

    public LimbDamageSource(Holder<DamageType> type, @Nullable Entity directEntity, @Nullable Entity causingEntity, String... bodyParts) {
        super(type, directEntity, causingEntity);
        this.bodyParts = bodyParts;
    }

    public LimbDamageSource(Holder<DamageType> type, Vec3 damageSourcePosition, String... bodyParts) {
        super(type, damageSourcePosition);
        this.bodyParts = bodyParts;
    }

    public LimbDamageSource(Holder<DamageType> type, @Nullable Entity entity, String... bodyParts) {
        super(type, entity);
        this.bodyParts = bodyParts;
    }

    public LimbDamageSource(Holder<DamageType> type, String... bodyParts) {
        super(type);
        this.bodyParts = bodyParts;
    }

    public void setAllowDeadBodyPartDamage(boolean allowDeadBodyPartDamage) {
        this.allowDeadBodyPartDamage = allowDeadBodyPartDamage;
    }

    @Override
    public String[] getLimbs() {
        return bodyParts;
    }

    @Override
    public boolean canDamageDeadLimbs() {
        return this.allowDeadBodyPartDamage;
    }
}
