package tnt.tarkovcraft.medsystem.common.effect.event.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventFunctions;

public record StatusScaleEventFunction(boolean localDamage, float scale) implements StatusEffectEventFunction {

    public static final MapCodec<StatusScaleEventFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("local_damage", true).forGetter(StatusScaleEventFunction::localDamage),
            Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(StatusScaleEventFunction::scale)
    ).apply(instance, StatusScaleEventFunction::new));

    @Override
    public float apply(float value, StatusEffectEventContext context) {
        float damageSrc = context.getDamage(this.localDamage, 0.0F);
        float amount = damageSrc * this.scale;
        return amount * value;
    }

    @Override
    public StatusEffectEventFunctionType<?> getType() {
        return MedSystemStatusEffectEventFunctions.DAMAGE_SCALE.value();
    }
}
