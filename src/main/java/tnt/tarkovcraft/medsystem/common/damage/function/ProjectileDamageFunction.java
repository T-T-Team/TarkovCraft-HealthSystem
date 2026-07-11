package tnt.tarkovcraft.medsystem.common.damage.function;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.ExtraCodecs;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculator;
import tnt.tarkovcraft.medsystem.common.health.calc.ProjectileHitCalculator;

public record ProjectileDamageFunction(float pierceDecay) implements DamageFunction {

    public static final MapCodec<ProjectileDamageFunction> CODEC = ExtraCodecs.NON_NEGATIVE_FLOAT
            .xmap(ProjectileDamageFunction::new, ProjectileDamageFunction::pierceDecay).fieldOf("pierce_decay");

    @Override
    public HitCalculator resolve(HitCalculationContext context) {
        return new ProjectileHitCalculator(this.pierceDecay);
    }

    @Override
    public MapCodec<? extends DamageFunction> codec() {
        return CODEC;
    }
}
