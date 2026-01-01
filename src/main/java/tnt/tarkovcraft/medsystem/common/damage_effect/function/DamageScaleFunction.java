package tnt.tarkovcraft.medsystem.common.damage_effect.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffect;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContextType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectFunctions;

public record DamageScaleFunction(boolean localDamage, float scale) implements DamageEffectFunction {

    public static final MapCodec<DamageScaleFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("local_damage", true).forGetter(DamageScaleFunction::localDamage),
            Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(DamageScaleFunction::scale)
    ).apply(instance, DamageScaleFunction::new));

    @Override
    public int apply(int value, DamageEffectContext context) {
        float damageSrc = context.getDamage(this.localDamage);
        float amount = damageSrc * this.scale;
        return Mth.ceil(amount * value);
    }

    @Override
    public DamageEffectFunctionType<?> getType() {
        return MedSystemDamageEffectFunctions.DAMAGE_SCALE.value();
    }

    @Override
    public void validate(DamageEffectContextType type) {
        DamageEffect.validateContext(this, type, DamageEffectContextType.ON_HURT);
    }
}
