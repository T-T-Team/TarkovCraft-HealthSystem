package tnt.tarkovcraft.medsystem.common.health_event.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventContext;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventResult;

public record StatusRangeEventCondition(boolean localDamage, float from, float to) implements HealthEventCondition {

    public static final MapCodec<StatusRangeEventCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("local_damage", true).forGetter(StatusRangeEventCondition::localDamage),
            Codec.FLOAT.optionalFieldOf("from", -Float.MAX_VALUE).forGetter(StatusRangeEventCondition::from),
            Codec.FLOAT.optionalFieldOf("to", Float.MAX_VALUE).forGetter(StatusRangeEventCondition::to)
    ).apply(instance, StatusRangeEventCondition::new));

    @Override
    public HealthEventResult test(HealthEventContext ctx) {
        float damage = ctx.getDamage(this.localDamage, 0.0F);
        return HealthEventResult.condition(damage >= this.from && damage <= this.to);
    }

    @Override
    public MapCodec<? extends HealthEventCondition> codec() {
        return CODEC;
    }
}
