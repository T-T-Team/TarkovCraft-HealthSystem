package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemUseAnimation;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.heal.HealItemAttributes;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.effect.PainReliefEffect;
import tnt.tarkovcraft.medsystem.common.item.BloodBagItem;
import tnt.tarkovcraft.medsystem.common.item.HealingItem;
import tnt.tarkovcraft.medsystem.common.status.BloodContainer;

public final class MedSystemItems {

    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(MedicalSystem.MOD_ID);

    public static final DeferredItem<HealingItem> EMERGENCY_SURGERY_KIT = REGISTRY.registerItem(
            "emergency_surgery_kit",
            properties -> new HealingItem(
                    properties.durability(10)
                            .setNoCombineRepair()
                            .component(DataComponents.BREAK_SOUND, null)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealItemAttributes.builder()
                                    .surgeryItem(builder -> builder
                                            .useTime(Duration.seconds(15))
                                            .recoverHealth(1.0F)
                                            .minLimbHealth(1.0F)
                                            .recovery(Duration.minutes(15), 0.7F)
                                    )
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
                    ItemUseAnimation.EAT,
                    properties.durability(4)
                            .setNoCombineRepair()
                            .component(DataComponents.BREAK_SOUND, null)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealItemAttributes.withSideEffectsOnly(Duration.seconds(3)))
                            .component(MedSystemItemComponents.SIDE_EFFECTS, SideEffectHolder.withItemUsage()
                                    .delayed(Duration.minutes(10), Duration.seconds(45), PainReliefEffect.createTemplate())
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
    public static final DeferredItem<BloodBagItem> BLOODBAG = REGISTRY.registerItem(
            "bloodbag",
            properties -> new BloodBagItem(
                    properties.stacksTo(1)
                            .component(MedSystemItemComponents.BLOOD_CONTAINER, new BloodContainer(0.5F, 0.0F, true))
            )
    );
}
