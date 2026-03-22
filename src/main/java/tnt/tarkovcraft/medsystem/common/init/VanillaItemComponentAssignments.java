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
        // TODO consume event
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
