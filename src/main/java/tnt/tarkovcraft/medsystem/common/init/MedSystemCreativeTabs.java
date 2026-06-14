package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;

import java.util.function.UnaryOperator;

public final class MedSystemCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MedSystemConstants.MOD_ID);

    public static final Holder<CreativeModeTab> MEDICAL_TAB = register("medical", builder -> builder
            .icon(MedSystemItems.FIRST_AID_KIT::toStack)
            .displayItems((parameters, output) -> {
                output.accept(MedSystemItems.EMERGENCY_SURGERY_KIT);
                output.accept(MedSystemItems.BANDAGE);
                output.accept(MedSystemItems.TOURNIQUET);
                output.accept(MedSystemItems.SPLINT);
                output.accept(MedSystemItems.PAINKILLERS);
                output.accept(MedSystemItems.FIRST_AID_KIT);
                output.accept(MedSystemItems.BLOODBAG);
                output.accept(MedSystemItems.MORPHINE_INJECTOR);
                output.accept(MedSystemItems.REGENERATIVE_INJECTOR);
            })
    );

    private static Holder<CreativeModeTab> register(String name, UnaryOperator<CreativeModeTab.Builder> builder) {
        return REGISTRY.register(name, key -> {
            CreativeModeTab.Builder tabBuilder = CreativeModeTab.builder().title(Component.translatable(key.toLanguageKey("itemGroup")));
            return builder.apply(tabBuilder)
                    .build();
        });
    }
}
