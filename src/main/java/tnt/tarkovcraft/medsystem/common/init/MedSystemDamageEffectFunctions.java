package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.damage_effect.function.CalculateFunction;
import tnt.tarkovcraft.medsystem.common.damage_effect.function.DamageEffectFunctionType;
import tnt.tarkovcraft.medsystem.common.damage_effect.function.DamageScaleFunction;
import tnt.tarkovcraft.medsystem.common.damage_effect.function.DeadLimbScaleFunction;

public final class MedSystemDamageEffectFunctions {

    public static final DeferredRegister<DamageEffectFunctionType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.DAMAGE_EFFECT_FUNCTION, MedSystemConstants.MOD_ID);

    public static final Holder<DamageEffectFunctionType<?>> DAMAGE_SCALE = REGISTRY.register("damage_scale", key -> new DamageEffectFunctionType<>(key, DamageScaleFunction.CODEC));
    public static final Holder<DamageEffectFunctionType<?>> DEAD_LIMB_SCALE = REGISTRY.register("dead_limb_scale", key -> new DamageEffectFunctionType<>(key, DeadLimbScaleFunction.CODEC));
    public static final Holder<DamageEffectFunctionType<?>> CALCULATION = REGISTRY.register("calculation", key -> new DamageEffectFunctionType<>(key, CalculateFunction.CODEC));
}
