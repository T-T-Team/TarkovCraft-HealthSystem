package tnt.tarkovcraft.medsystem.common.init;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.consume_effect.RemoveShockConsumeEffect;

public final class MedSystemConsumeEffects {

    public static final DeferredRegister<ConsumeEffect.Type<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.CONSUME_EFFECT_TYPE, MedSystemConstants.MOD_ID);

    public static final Holder<ConsumeEffect.Type<?>> REMOVE_SHOCK = register("remove_shock", RemoveShockConsumeEffect.CODEC, RemoveShockConsumeEffect.STREAM_CODEC);

    private static <T extends ConsumeEffect> Holder<ConsumeEffect.Type<?>> register(String name, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> streamCodec) {
        return REGISTRY.register(name, () -> new ConsumeEffect.Type<>(codec, streamCodec));
    }
}
