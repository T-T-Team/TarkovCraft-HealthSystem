package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.reaction.function.ChanceFunctionType;
import tnt.tarkovcraft.medsystem.common.health.reaction.function.FallDistanceLimitFunction;
import tnt.tarkovcraft.medsystem.common.health.reaction.function.FallDistanceScaleFunction;

public final class MedSystemChanceFunctions {

    public static final DeferredRegister<ChanceFunctionType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.CHANCE_FUNCTION, MedicalSystem.MOD_ID);

    public static final Holder<ChanceFunctionType<?>> FALL_DISTANCE = REGISTRY.register("fall_distance", key -> new ChanceFunctionType<>(key, FallDistanceScaleFunction.CODEC));
    public static final Holder<ChanceFunctionType<?>> FALL_DISTANCE_LIMIT = REGISTRY.register("fall_distance_limit", key -> new ChanceFunctionType<>(key, FallDistanceLimitFunction.CODEC));
}
