package tnt.tarkovcraft.medsystem.api;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class BodyPartDamageSource extends DamageSource implements SpecificBodyPartDamage {

    private final String[] bodyParts;
    private boolean allowDeadBodyPartDamage = true;

    public BodyPartDamageSource(Holder<DamageType> type, @Nullable Entity directEntity, @Nullable Entity causingEntity, @Nullable Vec3 damageSourcePosition, String... bodyParts) {
        super(type, directEntity, causingEntity, damageSourcePosition);
        this.bodyParts = bodyParts;
    }

    public BodyPartDamageSource(Holder<DamageType> type, @Nullable Entity directEntity, @Nullable Entity causingEntity, String... bodyParts) {
        super(type, directEntity, causingEntity);
        this.bodyParts = bodyParts;
    }

    public BodyPartDamageSource(Holder<DamageType> type, Vec3 damageSourcePosition, String... bodyParts) {
        super(type, damageSourcePosition);
        this.bodyParts = bodyParts;
    }

    public BodyPartDamageSource(Holder<DamageType> type, @Nullable Entity entity, String... bodyParts) {
        super(type, entity);
        this.bodyParts = bodyParts;
    }

    public BodyPartDamageSource(Holder<DamageType> type, String... bodyParts) {
        super(type);
        this.bodyParts = bodyParts;
    }

    public void setAllowDeadBodyPartDamage(boolean allowDeadBodyPartDamage) {
        this.allowDeadBodyPartDamage = allowDeadBodyPartDamage;
    }

    @Override
    public String[] getBodyParts() {
        return bodyParts;
    }

    @Override
    public boolean allowDeadBodyPartDamage() {
        return this.allowDeadBodyPartDamage;
    }
}
