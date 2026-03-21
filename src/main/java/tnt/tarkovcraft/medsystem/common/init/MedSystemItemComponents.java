package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.api.heal.HealItemAttributes;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.armor.ArmorMaterial;
import tnt.tarkovcraft.medsystem.common.armor.ArmorRating;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodContainer;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodContainerMode;
import tnt.tarkovcraft.medsystem.common.item.InteractionTarget;

import java.util.function.Supplier;

public final class MedSystemItemComponents {

    public static final DeferredRegister.DataComponents REGISTRY = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MedSystemConstants.MOD_ID);

    public static final Supplier<DataComponentType<HealItemAttributes>> HEAL_ATTRIBUTES = REGISTRY.registerComponentType("heal_attributes", builder -> builder
            .persistent(HealItemAttributes.CODEC)
    );
    public static final Supplier<DataComponentType<InteractionTarget>> INTERACTION_TARGET = REGISTRY.registerComponentType("interaction_target", builder -> builder
            .persistent(InteractionTarget.CODEC)
            .networkSynchronized(InteractionTarget.STREAM_CODEC)
    );
    public static final Supplier<DataComponentType<SideEffectHolder>> SIDE_EFFECTS = REGISTRY.registerComponentType("side_effects", builder -> builder
            .persistent(SideEffectHolder.CODEC)
    );
    public static final Supplier<DataComponentType<BloodContainer>> BLOOD_CONTAINER = REGISTRY.registerComponentType("blood_container", builder -> builder
            .persistent(BloodContainer.CODEC)
            .networkSynchronized(BloodContainer.STREAM_CODEC)
    );
    public static final Supplier<DataComponentType<BloodContainerMode>> BLOOD_CONTAINER_MODE = REGISTRY.registerComponentType("blood_container_mode", builder -> builder
            .persistent(BloodContainerMode.CODEC)
            .networkSynchronized(BloodContainerMode.STREAM_CODEC)
    );
    public static final Supplier<DataComponentType<ArmorRating>> ARMOR_RATING = REGISTRY.registerComponentType("armor_rating", builder -> builder
            .persistent(ArmorRating.CODEC)
    );
    public static final Supplier<DataComponentType<ArmorMaterial>> ARMOR_MATERIAL = REGISTRY.registerComponentType("armor_material", builder -> builder
            .persistent(ArmorMaterial.CODEC)
    );
}
