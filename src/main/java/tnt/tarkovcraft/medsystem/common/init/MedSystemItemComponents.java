package tnt.tarkovcraft.medsystem.common.init;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.ArmorStat;
import tnt.tarkovcraft.medsystem.api.heal.HealItemAttributes;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;

import java.util.function.Supplier;

public final class MedSystemItemComponents {

    public static final DeferredRegister.DataComponents REGISTRY = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MedicalSystem.MOD_ID);

    public static final Supplier<DataComponentType<ArmorStat>> ARMOR_STAT = REGISTRY.registerComponentType("armor_stat", builder -> builder
            .persistent(ArmorStat.CODEC)
    );
    public static final Supplier<DataComponentType<HealItemAttributes>> HEAL_ATTRIBUTES = REGISTRY.registerComponentType("heal_attributes", builder -> builder
            .persistent(HealItemAttributes.CODEC)
    );
    public static final Supplier<DataComponentType<String>> SELECTED_BODY_PART = REGISTRY.registerComponentType("selected_body_part", builder -> builder
            .persistent(Codec.STRING)
    );
    public static final Supplier<DataComponentType<SideEffectHolder>> SIDE_EFFECTS = REGISTRY.registerComponentType("side_effects", builder -> builder
            .persistent(SideEffectHolder.CODEC)
    );
}
