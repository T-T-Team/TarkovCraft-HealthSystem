package tnt.tarkovcraft.medsystem.common.damage_effect.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.core.util.NumberOperator;
import tnt.tarkovcraft.medsystem.common.damage_effect.DamageEffectContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemDamageEffectFunctions;

import java.util.Set;

public record DeadLimbScaleFunction(Set<LimbType> limb, float scale, NumberOperator operator, NumberOperator limbOperator) implements DamageEffectFunction {

    public static final MapCodec<DeadLimbScaleFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codecs.enumSet(LimbType.CODEC).fieldOf("limb").forGetter(DeadLimbScaleFunction::limb),
            Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(DeadLimbScaleFunction::scale),
            NumberOperator.CODEC.optionalFieldOf("operator", NumberOperator.ADD).forGetter(DeadLimbScaleFunction::operator),
            NumberOperator.CODEC.optionalFieldOf("limb_operator", NumberOperator.MULTIPLY).forGetter(DeadLimbScaleFunction::limbOperator)
    ).apply(instance, DeadLimbScaleFunction::new));

    @Override
    public int apply(int value, DamageEffectContext context) {
        HealthContainer container = context.health();
        int deadLimbCount = (int) container.getLimbsAsStream()
                .filter(limb -> limb.isDead() && this.limb.contains(limb.getType()))
                .count();
        return Mth.floor(this.operator.applyAsDouble(value, this.limbOperator.applyAsDouble(deadLimbCount, this.scale)));
    }

    @Override
    public DamageEffectFunctionType<?> getType() {
        return MedSystemDamageEffectFunctions.DEAD_LIMB_SCALE.value();
    }
}
