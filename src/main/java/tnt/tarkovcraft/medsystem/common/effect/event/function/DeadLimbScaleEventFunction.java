package tnt.tarkovcraft.medsystem.common.effect.event.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import tnt.tarkovcraft.core.util.Codecs;
import tnt.tarkovcraft.core.util.NumberOperator;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;
import tnt.tarkovcraft.medsystem.common.health.LimbType;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventFunctions;

import java.util.Set;

public record DeadLimbScaleEventFunction(Set<LimbType> limb, float scale, NumberOperator operator, NumberOperator limbOperator) implements StatusEffectEventFunction {

    public static final MapCodec<DeadLimbScaleEventFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codecs.enumSet(LimbType.CODEC).fieldOf("limb").forGetter(DeadLimbScaleEventFunction::limb),
            Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(DeadLimbScaleEventFunction::scale),
            NumberOperator.CODEC.optionalFieldOf("operator", NumberOperator.ADD).forGetter(DeadLimbScaleEventFunction::operator),
            NumberOperator.CODEC.optionalFieldOf("limb_operator", NumberOperator.MULTIPLY).forGetter(DeadLimbScaleEventFunction::limbOperator)
    ).apply(instance, DeadLimbScaleEventFunction::new));

    @Override
    public float apply(float value, StatusEffectEventContext context) {
        HealthContainer container = context.getHealthContainer();
        int deadLimbCount = (int) container.getLimbsAsStream()
                .filter(limb -> limb.isDead() && this.limb.contains(limb.getType()))
                .count();
        return (float) this.operator.applyAsDouble(value, this.limbOperator.applyAsDouble(deadLimbCount, this.scale));
    }

    @Override
    public StatusEffectEventFunctionType<?> getType() {
        return MedSystemStatusEffectEventFunctions.DEAD_LIMB_SCALE.value();
    }
}
