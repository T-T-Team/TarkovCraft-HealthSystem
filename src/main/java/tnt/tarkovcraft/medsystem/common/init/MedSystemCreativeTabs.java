package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;

public final class MedSystemCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, MedicalSystem.MOD_ID);

    public static final Holder<CreativeModeTab> MEDICAL_TAB = REGISTRY.register("medical", key -> CreativeModeTab.builder()
            .title(Component.translatable(key.toLanguageKey("itemGroup")))
            .icon(MedSystemItems.FIRST_AID_KIT::toStack)
            .displayItems((parameters, output) -> {
                output.accept(MedSystemItems.EMERGENCY_SURGERY_KIT);
                output.accept(MedSystemItems.BANDAGE);
                output.accept(MedSystemItems.TOURNIQUET);
                output.accept(MedSystemItems.SPLINT);
                output.accept(MedSystemItems.PAINKILLERS);
                output.accept(MedSystemItems.FIRST_AID_KIT);
            })
            .build()
    );
}
