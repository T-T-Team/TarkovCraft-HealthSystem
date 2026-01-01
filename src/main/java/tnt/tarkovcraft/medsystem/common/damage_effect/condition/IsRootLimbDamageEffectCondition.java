package tnt.tarkovcraft.medsystem.common.damage_effect.condition;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectConditions;

public final class IsRootLimbDamageEffectCondition implements DamageEffectCondition {

    public static final IsRootLimbDamageEffectCondition INSTANCE = new IsRootLimbDamageEffectCondition();
    public static final MapCodec<IsRootLimbDamageEffectCondition> CODEC = MapCodec.unit(INSTANCE);

    private IsRootLimbDamageEffectCondition() {
    }

    @Override
    public boolean matches(DamageEffectContext context) {
        Limb limb = context.limb();
        HealthContainer container = context.health();
        return container.getRootLimbCode().equals(limb.getLimbCode());
    }

    @Override
    public DamageEffectConditionType<?> getType() {
        return MedSystemDamageEffectConditions.IS_ROOT_LIMB.value();
    }
}
