package tnt.tarkovcraft.medsystem.common.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.ArmorStat;
import tnt.tarkovcraft.medsystem.api.heal.HealItemAttributes;

import java.util.function.Supplier;

public final class MedSystemItemComponents {

    public static final DeferredRegister<DataComponentType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, MedicalSystem.MOD_ID);

    public static final Supplier<DataComponentType<ArmorStat>> ARMOR_STAT = REGISTRY.register("armor_stat", () -> DataComponentType.<ArmorStat>builder()
            .persistent(ArmorStat.CODEC)
            .build()
    );
    public static final Supplier<DataComponentType<HealItemAttributes>> HEAL_ATTRIBUTES = REGISTRY.register("heal_attributes", () -> DataComponentType.<HealItemAttributes>builder()
            .persistent(HealItemAttributes.CODEC)
            .build()
    );
    public static final Supplier<DataComponentType<String>> SELECTED_BODY_PART = REGISTRY.register("selected_body_part", () -> DataComponentType.<String>builder()
            .persistent(Codec.STRING)
            .build()
    );
}
