package tnt.tarkovcraft.medsystem.common.damage.function;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import tnt.tarkovcraft.medsystem.common.health.calc.ExplosionHitCalculator;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculator;

public record ExplosionDamageFunction(float damageScale, float airPressure, float waterPressure) implements DamageFunction {

    public static final MapCodec<ExplosionDamageFunction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("damage_scale", 2.5F).forGetter(ExplosionDamageFunction::damageScale),
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("air_pressure", 0.5F).forGetter(ExplosionDamageFunction::airPressure),
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("water_pressure", 1.2F).forGetter(ExplosionDamageFunction::waterPressure)
    ).apply(instance, ExplosionDamageFunction::new));

    @Override
    public HitCalculator resolve(HitCalculationContext context) {
        return new ExplosionHitCalculator(this.damageScale, this.airPressure, this.waterPressure);
    }

    @Override
    public MapCodec<? extends DamageFunction> codec() {
        return CODEC;
    }
}
