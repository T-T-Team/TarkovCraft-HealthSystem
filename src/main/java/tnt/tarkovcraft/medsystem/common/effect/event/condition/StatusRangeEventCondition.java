package tnt.tarkovcraft.medsystem.common.effect.event.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventContext;
import tnt.tarkovcraft.medsystem.common.effect.event.TriggerResult;
import tnt.tarkovcraft.medsystem.common.init.MedSystemStatusEffectEventConditions;

public record StatusRangeEventCondition(boolean localDamage, float from, float to) implements StatusEffectEventCondition {

    public static final MapCodec<StatusRangeEventCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("local_damage", true).forGetter(StatusRangeEventCondition::localDamage),
            Codec.FLOAT.optionalFieldOf("from", -Float.MAX_VALUE).forGetter(StatusRangeEventCondition::from),
            Codec.FLOAT.optionalFieldOf("to", Float.MAX_VALUE).forGetter(StatusRangeEventCondition::to)
    ).apply(instance, StatusRangeEventCondition::new));

    @Override
    public TriggerResult test(StatusEffectEventContext ctx) {
        float damage = ctx.getDamage(this.localDamage, 0.0F);
        return TriggerResult.condition(damage >= this.from && damage <= this.to);
    }

    @Override
    public StatusEffectEventConditionType<?> getType() {
        return MedSystemStatusEffectEventConditions.DAMAGE_RANGE.value();
    }
}
