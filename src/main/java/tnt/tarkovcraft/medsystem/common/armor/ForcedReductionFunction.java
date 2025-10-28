package tnt.tarkovcraft.medsystem.common.armor;

import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.common.damagesource.IReductionFunction;

public record ForcedReductionFunction(float value) implements IReductionFunction {

    @Override
    public float modify(DamageContainer container, float reductionIn) {
        return this.value();
    }
}
