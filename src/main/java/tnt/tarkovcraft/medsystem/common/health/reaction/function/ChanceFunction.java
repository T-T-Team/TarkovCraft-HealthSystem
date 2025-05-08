package tnt.tarkovcraft.medsystem.common.health.reaction.function;

import tnt.tarkovcraft.core.util.context.Context;

public interface ChanceFunction {

    float apply(float chance, Context context);

    ChanceFunctionType<?> getType();
}
