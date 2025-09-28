package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import tnt.tarkovcraft.core.common.data.duration.Duration;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.api.heal.SideEffectHolder;
import tnt.tarkovcraft.medsystem.common.config.StatusEffectConfig;
import tnt.tarkovcraft.medsystem.common.effect.group.BloodRecoveryEffectGroupItem;

import java.util.function.BiConsumer;

public final class VanillaItemComponentAssignments {

    public static void adjustItemData(BiConsumer<ItemLike, SideEffectHolder> registration) {
        MedicalSystem.LOGGER.debug(MedicalSystem.MARKER, "Applying consumption/hit effects to vanilla items");
        StatusEffectConfig config = MedicalSystem.getConfig().statusEffects;
        int effectDuration = config.itemStatusEffectDuration;
        SideEffectHolder swords = config.swordStatusEffects.apply(SideEffectHolder.builder(), effectDuration)
                .title(SideEffectHolder.ITEM_TITLE)
                .build();
        registration.accept(Items.WOODEN_SWORD, swords);
        registration.accept(Items.STONE_SWORD, swords);
        registration.accept(Items.IRON_SWORD, swords);
        registration.accept(Items.GOLDEN_SWORD, swords);
        registration.accept(Items.DIAMOND_SWORD, swords);
        registration.accept(Items.NETHERITE_SWORD, swords);

        SideEffectHolder axes = config.axeStatusEffects.apply(SideEffectHolder.builder(), effectDuration)
                .title(SideEffectHolder.ITEM_TITLE)
                .build();
        registration.accept(Items.WOODEN_AXE, axes);
        registration.accept(Items.STONE_AXE, axes);
        registration.accept(Items.IRON_AXE, axes);
        registration.accept(Items.GOLDEN_AXE, axes);
        registration.accept(Items.DIAMOND_AXE, axes);
        registration.accept(Items.NETHERITE_AXE, axes);

        SideEffectHolder blunt = config.bluntStatusEffects.apply(SideEffectHolder.builder(), effectDuration)
                .title(SideEffectHolder.ITEM_TITLE)
                .build();
        registration.accept(Items.WOODEN_SHOVEL, blunt);
        registration.accept(Items.STONE_SHOVEL, blunt);
        registration.accept(Items.IRON_SHOVEL, blunt);
        registration.accept(Items.GOLDEN_SHOVEL, blunt);
        registration.accept(Items.DIAMOND_SHOVEL, blunt);
        registration.accept(Items.NETHERITE_SHOVEL, blunt);
        registration.accept(Items.WOODEN_PICKAXE, blunt);
        registration.accept(Items.STONE_PICKAXE, blunt);
        registration.accept(Items.IRON_PICKAXE, blunt);
        registration.accept(Items.GOLDEN_PICKAXE, blunt);
        registration.accept(Items.DIAMOND_PICKAXE, blunt);
        registration.accept(Items.NETHERITE_PICKAXE, blunt);
        registration.accept(Items.WOODEN_HOE, blunt);
        registration.accept(Items.STONE_HOE, blunt);
        registration.accept(Items.IRON_HOE, blunt);
        registration.accept(Items.GOLDEN_HOE, blunt);
        registration.accept(Items.DIAMOND_HOE, blunt);
        registration.accept(Items.NETHERITE_HOE, blunt);
        registration.accept(Items.MACE, blunt);

        // Foods
        SideEffectHolder bloodRegeneration = SideEffectHolder.builder()
                .buffs(builder ->
                        builder.create(Duration.minutes(2), Duration.minutes(1), new BloodRecoveryEffectGroupItem(0.002F))
                )
                .build();
        registration.accept(Items.PUMPKIN_PIE, bloodRegeneration);
        registration.accept(Items.COOKED_COD, bloodRegeneration);
        registration.accept(Items.COOKED_SALMON, bloodRegeneration);
        registration.accept(Items.BEETROOT_SOUP, bloodRegeneration);
        registration.accept(Items.MUSHROOM_STEW, bloodRegeneration);
        registration.accept(Items.GOLDEN_APPLE, bloodRegeneration);
        registration.accept(Items.ENCHANTED_GOLDEN_APPLE, bloodRegeneration);
        registration.accept(Items.GOLDEN_CARROT, bloodRegeneration);
    }
}
