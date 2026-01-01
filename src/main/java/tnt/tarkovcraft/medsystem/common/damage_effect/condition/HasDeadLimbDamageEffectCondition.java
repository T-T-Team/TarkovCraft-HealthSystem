package tnt.tarkovcraft.medsystem.common.damage_effect.condition;

import com.mojang.serialization.MapCodec;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectConditions;

import java.util.Set;

public record HasDeadLimbDamageEffectCondition(Set<LimbType> limb) implements DamageEffectCondition {

    public static final MapCodec<HasDeadLimbDamageEffectCondition> CODEC = Codecs.enumSet(LimbType.CODEC).fieldOf("limb")
            .xmap(HasDeadLimbDamageEffectCondition::new, HasDeadLimbDamageEffectCondition::limb);

    @Override
    public boolean matches(DamageEffectContext context) {
        HealthContainer container = context.health();
        return container.getLimbsAsStream()
                .anyMatch(limb -> limb.isDead() && this.limb.contains(limb.getType()));
    }

    @Override
    public DamageEffectConditionType<?> getType() {
        return MedSystemDamageEffectConditions.HAS_DEAD_LIMB.value();
    }
}
