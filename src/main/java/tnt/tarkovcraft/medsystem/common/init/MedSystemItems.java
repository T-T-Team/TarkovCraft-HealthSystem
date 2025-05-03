package tnt.tarkovcraft.medsystem.common.init;

import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.core.common.data.Duration;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.HealAttributes;
import tnt.tarkovcraft.medsystem.common.item.HealingItem;

public final class MedSystemItems {

    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(MedicalSystem.MOD_ID);

    public static final DeferredItem<HealingItem> EMERGENCY_SURGERY_KIT = REGISTRY.registerItem(
            "emergency_surgery_kit",
            properties -> new HealingItem(
                    Duration.seconds(15),
                    properties.durability(5)
                            .setNoCombineRepair()
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealAttributes.builder()
                                    .deadLimbHealing(1.0F, 0.6F)
                                    .build()
                            )
            )
    );
}
