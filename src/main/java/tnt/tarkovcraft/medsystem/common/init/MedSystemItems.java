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
                    properties.durability(5)
                            .setNoCombineRepair()
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealAttributes.builder()
                                    .surgeryItem()
                                        .useTime(Duration.seconds(15))
                                        .recoverHealth(1.0F)
                                        .minLimbHealth(1.0F)
                                        .recovery(Duration.minutes(5), 0.7F)
                                        .buildSurgeryAttributes()
                                    .build()
                            )
            )
    );
    public static final DeferredItem<HealingItem> BANDAGE = REGISTRY.registerItem(
            "bandage",
            properties -> new HealingItem(
                    properties.durability(1)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealAttributes.builder()
                                    .removesEffect(1, MedSystemStatusEffects.LIGHT_BLEED)
                                    .build()
                            )
            )
    );
    public static final DeferredItem<HealingItem> TOURNIQUET = REGISTRY.registerItem(
            "tourniquet",
            properties -> new HealingItem(
                    properties.durability(1)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealAttributes.builder()
                                    .removesEffect(1, MedSystemStatusEffects.HEAVY_BLEED)
                                    .build()
                            )
            )
    );
    public static final DeferredItem<HealingItem> SPLINT = REGISTRY.registerItem(
            "splint",
            properties -> new HealingItem(
                    properties.durability(1)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealAttributes.builder()
                                    .removesEffect(1, MedSystemStatusEffects.FRACTURE)
                                    .build()
                            )
            )
    );
    public static final DeferredItem<HealingItem> PAINKILLERS = REGISTRY.registerItem(
            "painkillers",
            properties -> new HealingItem(
                    properties.durability(4)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealAttributes.builder()
                                    .setNoBodyPartSelection()
                                    .sideEffect(1.0F, Duration.minutes(5), MedSystemStatusEffects.PAIN_RELIEF)
                                    .build()
                            )
            )
    );
    public static final DeferredItem<HealingItem> FIRST_AID_KIT = REGISTRY.registerItem(
            "first_aid_kit",
            properties -> new HealingItem(
                    properties.durability(20)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealAttributes.builder()
                                    .unrestrictedHealing(20, 1)
                                    .removesEffect(3, MedSystemStatusEffects.LIGHT_BLEED)
                                    .build()
                            )
            )
    );
}
