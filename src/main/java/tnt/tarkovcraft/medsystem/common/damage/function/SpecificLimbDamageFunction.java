package tnt.tarkovcraft.medsystem.common.damage.function;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.damagesource.DamageSource;
import tnt.tarkovcraft.medsystem.api.SpecificLimbDamage;
import tnt.tarkovcraft.medsystem.common.health.calc.GenericHitCalculator;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculator;
import tnt.tarkovcraft.medsystem.common.health.calc.SpecificBodyPartHitCalculator;

public final class SpecificLimbDamageFunction implements DamageFunction {

    public static final MapCodec<SpecificLimbDamageFunction> CODEC = MapCodec.unit(SpecificLimbDamageFunction::new);

    @Override
    public HitCalculator resolve(HitCalculationContext context) {
        DamageSource source = context.source();
        if (source instanceof SpecificLimbDamage specificLimbDamage) {
            return new SpecificBodyPartHitCalculator(specificLimbDamage);
        }
        return GenericHitCalculator.INSTANCE;
    }

    @Override
    public MapCodec<? extends DamageFunction> codec() {
        return CODEC;
    }
}
