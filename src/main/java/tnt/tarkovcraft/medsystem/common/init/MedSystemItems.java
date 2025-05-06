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
                    properties.durability(20)
                            .setNoCombineRepair()
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealAttributes.builder()
                                    .surgeryItem()
                                        .useTime(Duration.seconds(10))
                                        .recoverHealth(1.0F)
                                        .minLimbHealth(1.0F)
                                        .recovery(Duration.minutes(10), 0.7F)
                                        .buildSurgeryAttributes()
                                    .healing(40, 2, 2.0F)
                                    .removesEffect(3, MedSystemStatusEffects.FRACTURE)
                                    .sideEffect(0.5F, Duration.seconds(30), MedSystemStatusEffects.PAIN_RELIEF)
                                    .build()
                            )
            )
    );
}
