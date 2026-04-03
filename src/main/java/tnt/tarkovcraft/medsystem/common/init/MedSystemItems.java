package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.UseAnim;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.core.common.init.CoreItemDataComponents;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.api.heal.HealItemAttributes;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.api.heal.predicate.IsBleedPredicate;
import tnt.tarkovcraft.medsystem.common.blood_system.BloodContainer;
import tnt.tarkovcraft.medsystem.common.effect.BleedStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.ConcussionStatusEffect;
import tnt.tarkovcraft.medsystem.common.effect.PainReliefEffect;
import tnt.tarkovcraft.medsystem.common.effect.group.HealthEffectGroupItem;
import tnt.tarkovcraft.medsystem.common.effect.group.MobEffectGroupItem;
import tnt.tarkovcraft.medsystem.common.item.BloodBagItem;
import tnt.tarkovcraft.medsystem.common.item.HealingItem;
import tnt.tarkovcraft.medsystem.common.item.SimpleHealingItem;

public final class MedSystemItems {

    public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(MedSystemConstants.MOD_ID);

    public static final DeferredItem<HealingItem> EMERGENCY_SURGERY_KIT = REGISTRY.registerItem(
            "emergency_surgery_kit",
            properties -> new HealingItem(
                    properties.durability(10)
                            .setNoRepair()
                            .component(CoreItemDataComponents.WEIGHT, 1000)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealItemAttributes.builder()
                                    .surgeryItem(builder -> builder
                                            .useTime(Duration.seconds(15))
                                            .recoversTo(1.0F)
                                            .postSurgeryRecovery(Duration.minutes(15), 0.7F)
                                    )
                                    .build()
                            )
            )
    );
    public static final DeferredItem<HealingItem> BANDAGE = REGISTRY.registerItem(
            "bandage",
            properties -> new HealingItem(
                    properties
                            .component(CoreItemDataComponents.WEIGHT, 150)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealItemAttributes.builder()
                                    .setMinUseTime(Duration.seconds(2))
                                    .removesEffect(MedSystemStatusEffects.BLEED, new IsBleedPredicate(false), BleedStatusEffect.LIGHT_BLEED)
                                    .build()
                            )
            )
    );
    public static final DeferredItem<HealingItem> TOURNIQUET = REGISTRY.registerItem(
            "tourniquet",
            properties -> new HealingItem(
                    properties
                            .component(CoreItemDataComponents.WEIGHT, 250)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealItemAttributes.builder()
                                    .setMinUseTime(Duration.seconds(3))
                                    .removesEffect(MedSystemStatusEffects.BLEED, new IsBleedPredicate(true), BleedStatusEffect.HEAVY_BLEED)
                                    .build()
                            )
            )
    );
    public static final DeferredItem<HealingItem> SPLINT = REGISTRY.registerItem(
            "splint",
            properties -> new HealingItem(
                    properties
                            .component(CoreItemDataComponents.WEIGHT, 600)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealItemAttributes.builder()
                                    .setMinUseTime(Duration.seconds(5))
                                    .removesEffect(MedSystemStatusEffects.FRACTURE)
                                    .build()
                            )
            )
    );
    public static final DeferredItem<SimpleHealingItem> PAINKILLERS = REGISTRY.registerItem(
            "painkillers",
            properties -> new SimpleHealingItem(
                    properties.durability(4)
                            .setNoRepair()
                            .component(CoreItemDataComponents.WEIGHT, 50)
                            .component(MedSystemItemComponents.SIDE_EFFECTS, SideEffectHolder.withItemUsage()
                                    .delayed(Duration.minutes(10), Duration.seconds(45), PainReliefEffect.createTemplate())
                                    .build()
                            )
            ).withUseAnimations(UseAnim.EAT, UseAnim.BOW)
    );
    public static final DeferredItem<HealingItem> FIRST_AID_KIT = REGISTRY.registerItem(
            "first_aid_kit",
            properties -> new HealingItem(
                    properties.durability(30)
                            .component(CoreItemDataComponents.WEIGHT, 750)
                            .component(MedSystemItemComponents.HEAL_ATTRIBUTES, HealItemAttributes.builder()
                                    .unrestrictedHealing(20, 2)
                                    .removesEffect(4, MedSystemStatusEffects.BLEED, new IsBleedPredicate(false), BleedStatusEffect.LIGHT_BLEED)
                                    .build()
                            )
            )
    );
    public static final DeferredItem<BloodBagItem> BLOODBAG = REGISTRY.registerItem(
            "bloodbag",
            properties -> new BloodBagItem(
                    properties.stacksTo(1)
                            .component(CoreItemDataComponents.WEIGHT, 100)
                            .component(MedSystemItemComponents.BLOOD_CONTAINER, BloodContainer.emptyContainer(0.5F, true))
            )
    );
    public static final DeferredItem<SimpleHealingItem> MORPHINE_INJECTOR = REGISTRY.registerItem(
            "morphine_injector",
            properties -> new SimpleHealingItem(
                    properties.durability(1)
                            .component(CoreItemDataComponents.WEIGHT, 80)
                            .component(MedSystemItemComponents.SIDE_EFFECTS, SideEffectHolder.withItemUsage()
                                    .delayed(Duration.minutes(10), Duration.seconds(3), PainReliefEffect.createTemplate())
                                    .buffs(factory -> {
                                        factory.create(Duration.seconds(10), Duration.seconds(3), new HealthEffectGroupItem("f8994779-eefd-48bc-a274-d27af57ef6d3", 0.5F));
                                    })
                                    .debuffs(factory -> {
                                        factory.create(Duration.minutes(3), Duration.minutes(10).addSeconds(3), new MobEffectGroupItem(MobEffects.WEAKNESS));
                                    })
                                    .build()
                            )
            )
    );
    public static final DeferredItem<SimpleHealingItem> REGENERATIVE_INJECTOR = REGISTRY.registerItem(
            "regenerative_injector",
            properties -> new SimpleHealingItem(
                    properties.durability(1)
                            .component(CoreItemDataComponents.WEIGHT, 80)
                            .component(MedSystemItemComponents.SIDE_EFFECTS, SideEffectHolder.withItemUsage()
                                    .buffs(factory -> {
                                        factory.create(Duration.minutes(1).addSeconds(30), Duration.seconds(3), new HealthEffectGroupItem("8dfbb21e-2156-4b54-82aa-415b341319fe", 0.25F, 10));
                                    })
                                    .delayed(0.4F, Duration.seconds(10), Duration.minutes(5), ConcussionStatusEffect.createTemplate())
                                    .debuffs(factory -> {
                                        factory.create(Duration.minutes(1), Duration.minutes(5), new MobEffectGroupItem(MobEffects.HUNGER, 1));
                                    })
                                    .build()
                            )
            )
    );
}
