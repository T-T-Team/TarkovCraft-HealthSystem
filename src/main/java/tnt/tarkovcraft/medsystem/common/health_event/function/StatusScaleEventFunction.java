package tnt.tarkovcraft.medsystem.common.health_event.function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;

public record StatusScaleEventFunction(boolean localDamage, float scale) implements HealthEventFunction {

    public static final MapCodec<StatusScaleEventFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("local_damage", true).forGetter(StatusScaleEventFunction::localDamage),
            Codec.FLOAT.optionalFieldOf("scale", 1.0F).forGetter(StatusScaleEventFunction::scale)
    ).apply(instance, StatusScaleEventFunction::new));

    @Override
    public float apply(float value, HealthEventContext context) {
        float damageSrc = context.getDamage(this.localDamage, 0.0F);
        float amount = damageSrc * this.scale;
        return amount * value;
    }

    @Override
    public MapCodec<? extends HealthEventFunction> codec() {
        return CODEC;
    }
}
