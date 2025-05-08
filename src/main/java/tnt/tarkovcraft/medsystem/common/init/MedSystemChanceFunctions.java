package tnt.tarkovcraft.medsystem.common.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.reaction.function.ChanceFunctionType;
import tnt.tarkovcraft.medsystem.common.health.reaction.function.FallDistanceScaleFunction;

import java.util.function.Supplier;

public final class MedSystemChanceFunctions {

    public static final DeferredRegister<ChanceFunctionType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.CHANCE_FUNCTION, MedicalSystem.MOD_ID);

    public static final Supplier<ChanceFunctionType<FallDistanceScaleFunction>> FALL_DISTANCE = REGISTRY.register("fall_distance", key -> new ChanceFunctionType<>(key, FallDistanceScaleFunction.CODEC));
}
