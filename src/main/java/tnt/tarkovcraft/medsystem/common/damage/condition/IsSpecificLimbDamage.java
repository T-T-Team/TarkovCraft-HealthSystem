package tnt.tarkovcraft.medsystem.common.damage.condition;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.damagesource.DamageSource;
import tnt.tarkovcraft.medsystem.api.SpecificLimbDamage;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;

public final class IsSpecificLimbDamage implements DamageCondition {

    public static final MapCodec<IsSpecificLimbDamage> CODEC = MapCodec.unit(IsSpecificLimbDamage::new);

    @Override
    public boolean test(HitCalculationContext context) {
        DamageSource source = context.source();
        return source instanceof SpecificLimbDamage;
    }

    @Override
    public MapCodec<? extends DamageCondition> codec() {
        return CODEC;
    }
}
