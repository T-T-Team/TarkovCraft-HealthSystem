package tnt.tarkovcraft.medsystem.common.damage_effect.condition;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.health.Limb;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectConditions;

import java.util.List;

public record IsLimbTypeDamageEffectCondition(List<LimbType> limbs) implements DamageEffectCondition {

    public static final MapCodec<IsLimbTypeDamageEffectCondition> CODEC = Codecs.list(LimbType.CODEC)
            .xmap(IsLimbTypeDamageEffectCondition::new, IsLimbTypeDamageEffectCondition::limbs).fieldOf("limb");

    @Override
    public boolean matches(DamageEffectContext context) {
        Limb limb = context.limb();
        return this.limbs.contains(limb.getType());
    }

    @Override
    public DamageEffectConditionType<?> getType() {
        return MedSystemDamageEffectConditions.IS_LIMB.value();
    }
}
