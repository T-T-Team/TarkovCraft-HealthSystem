package tnt.tarkovcraft.medsystem.common.damage_effect.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffect;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContextType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectConditions;

public record DamageRangeCondition(boolean localDamage, float from, float to) implements DamageEffectCondition {

    public static final MapCodec<DamageRangeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("local_damage", true).forGetter(DamageRangeCondition::localDamage),
            Codec.FLOAT.optionalFieldOf("from", -Float.MAX_VALUE).forGetter(DamageRangeCondition::from),
            Codec.FLOAT.optionalFieldOf("to", Float.MAX_VALUE).forGetter(DamageRangeCondition::to)
    ).apply(instance, DamageRangeCondition::new));

    @Override
    public boolean matches(DamageEffectContext context) {
        float damage = context.getDamage(this.localDamage);
        return damage >= this.from && damage <= this.to;
    }

    @Override
    public void validate(DamageEffectContextType contextType) {
        DamageEffect.validateContext(this, contextType, DamageEffectContextType.ON_HURT);
    }

    @Override
    public DamageEffectConditionType<?> getType() {
        return MedSystemDamageEffectConditions.DAMAGE_RANGE.value();
    }
}
