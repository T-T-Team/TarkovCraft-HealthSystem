package tnt.tarkovcraft.medsystem.common.init;

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

    public static final Holder<ArgumentTypeInfo<?, ?>> STATUS_EFFECT = REGISTRY.register("status_effect", () -> ArgumentTypeInfos.registerByClass(StatusEffectArgument.class, SingletonArgumentInfo.contextAware(StatusEffectArgument::statusEffect)));
}
