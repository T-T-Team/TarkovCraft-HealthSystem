package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.ArmorStat;

import java.util.function.Supplier;

public final class MedSystemItemComponents {

    public static final DeferredRegister<DataComponentType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, MedicalSystem.MOD_ID);

    public static final Supplier<DataComponentType<ArmorStat>> ARMOR_STAT = REGISTRY.register("armor_stat", () -> DataComponentType.<ArmorStat>builder()
            .persistent(ArmorStat.CODEC)
            .build()
    );
}
