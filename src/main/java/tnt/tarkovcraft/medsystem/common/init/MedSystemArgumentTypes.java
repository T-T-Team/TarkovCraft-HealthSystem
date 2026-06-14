package tnt.tarkovcraft.medsystem.common.init;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.argument.StatusEffectArgument;

public final class MedSystemArgumentTypes {

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> REGISTRY = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, MedSystemConstants.MOD_ID);

    public static final Holder<ArgumentTypeInfo<?, ?>> STATUS_EFFECT = register("status_effect", StatusEffectArgument.class, SingletonArgumentInfo.contextAware(StatusEffectArgument::statusEffect));

    private static <T extends ArgumentType<?>> Holder<ArgumentTypeInfo<?, ?>> register(String name, Class<T> type, ArgumentTypeInfo<T, ?> info) {
        return REGISTRY.register(name, () -> ArgumentTypeInfos.registerByClass(type, info));
    }
}
