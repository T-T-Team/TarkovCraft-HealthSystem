package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.common.damage_effect.function.DamageEffectFunctionType;
import tnt.tarkovcraft.medsystem.common.damage_effect.function.DamageScaleFunction;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;

public final class MedSystemDamageEffectScaleFunctions {

    public static final DeferredRegister<DamageEffectFunctionType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.DAMAGE_EFFECT_FUNCTION, MedSystemConstants.MOD_ID);

    public static final Holder<DamageEffectFunctionType<?>> DAMAGE_SCALE = REGISTRY.register("damage_scale", key -> new DamageEffectFunctionType<>(key, DamageScaleFunction.CODEC));
}
