package tnt.tarkovcraft.medsystem.common.damage_effect.condition;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectConditions;

public final class IsDeadLimbDamageEffectCondition implements DamageEffectCondition {

    public static final IsDeadLimbDamageEffectCondition INSTANCE = new IsDeadLimbDamageEffectCondition();
    public static final MapCodec<IsDeadLimbDamageEffectCondition> CODEC = MapCodec.unit(INSTANCE);

    private IsDeadLimbDamageEffectCondition() {}

    @Override
    public boolean matches(DamageEffectContext context) {
        return context.limb().isDead();
    }

    @Override
    public DamageEffectConditionType<?> getType() {
        return MedSystemDamageEffectConditions.IS_DEAD_LIMB.value();
    }
}
