package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.heal.HealItemAttributes;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.item.HealingItem;

public final class MedSystemItems {

    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(MedicalSystem.MOD_ID);

    public static final DeferredItem<HealingItem> EMERGENCY_SURGERY_KIT = REGISTRY.registerItem(
            "emergency_surgery_kit",
            properties -> new HealingItem(
                    properties.durability(5)
                            .setNoCombineRepair()
                            .component(DataComponents.BREAK_SOUND, null)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealItemAttributes.builder()
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
                    properties.component(DataComponents.BREAK_SOUND, null)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealItemAttributes.builder()
                                    .setMinUseTime(Duration.seconds(2))
                                    .removesEffect(MedSystemStatusEffects.LIGHT_BLEED)
                                    .build()
                            )
            )
    );
    public static final DeferredItem<HealingItem> TOURNIQUET = REGISTRY.registerItem(
            "tourniquet",
            properties -> new HealingItem(
                    properties.component(DataComponents.BREAK_SOUND, null)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealItemAttributes.builder()
                                    .setMinUseTime(Duration.seconds(3))
                                    .removesEffect(MedSystemStatusEffects.HEAVY_BLEED)
                                    .build()
                            )
            )
    );
    public static final DeferredItem<HealingItem> SPLINT = REGISTRY.registerItem(
            "splint",
            properties -> new HealingItem(
                    properties.component(DataComponents.BREAK_SOUND, null)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealItemAttributes.builder()
                                    .setMinUseTime(Duration.seconds(5))
                                    .removesEffect(MedSystemStatusEffects.FRACTURE)
                                    .build()
                            )
            )
    );
    public static final DeferredItem<HealingItem> PAINKILLERS = REGISTRY.registerItem(
            "painkillers",
            properties -> new HealingItem(
                    properties.durability(4)
                            .component(DataComponents.BREAK_SOUND, null)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealItemAttributes.builder()
                                    .setNoBodyPartSelection()
                                    .setMinUseTime(Duration.seconds(3))
                                    .setAlwaysConsumable()
                                    .build()
                            )
                            .component(MedSystemItemComponents.SIDE_EFFECTS, SideEffectHolder.builder()
                                    .sideEffect(1.0F, Duration.minutes(10), Duration.seconds(45), MedSystemStatusEffects.PAIN_RELIEF)
                                    .build()
                            )
            )
    );
    public static final DeferredItem<HealingItem> FIRST_AID_KIT = REGISTRY.registerItem(
            "first_aid_kit",
            properties -> new HealingItem(
                    properties.durability(30)
                            .component(DataComponents.BREAK_SOUND, null)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealItemAttributes.builder()
                                    .unrestrictedHealing(20, 2)
                                    .removesEffect(4, MedSystemStatusEffects.LIGHT_BLEED)
                                    .build()
                            )
            )
    );
}
