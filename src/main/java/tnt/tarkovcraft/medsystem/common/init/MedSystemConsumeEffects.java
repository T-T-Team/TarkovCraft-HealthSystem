package tnt.tarkovcraft.medsystem.common.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.consume_effect.ConsumeEffect;
import tnt.tarkovcraft.medsystem.common.consume_effect.ConsumeEffectType;
import tnt.tarkovcraft.medsystem.common.consume_effect.RemoveShockConsumeEffect;

public final class MedSystemConsumeEffects {

    public static final DeferredRegister<ConsumeEffectType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.CONSUME_EFFECT, MedSystemConstants.MOD_ID);

    public static final Holder<ConsumeEffectType<?>> REMOVE_SHOCK = register("remove_shock", RemoveShockConsumeEffect.CODEC);

    private static <T extends ConsumeEffect> Holder<ConsumeEffectType<?>> register(String name, MapCodec<T> codec) {
        return REGISTRY.register(name, id -> new ConsumeEffectType<>(id, codec));
    }
}
