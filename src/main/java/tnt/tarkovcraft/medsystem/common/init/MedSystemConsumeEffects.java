package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.consume_effect.ConsumeEffectType;
import tnt.tarkovcraft.medsystem.common.consume_effect.RemoveShockConsumeEffect;

public final class MedSystemConsumeEffects {

    public static final DeferredRegister<ConsumeEffectType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.CONSUME_EFFECT, MedSystemConstants.MOD_ID);

    public static final Holder<ConsumeEffectType<?>> REMOVE_SHOCK = REGISTRY.register("remove_shock", key -> new ConsumeEffectType<>(key, RemoveShockConsumeEffect.CODEC));
}
