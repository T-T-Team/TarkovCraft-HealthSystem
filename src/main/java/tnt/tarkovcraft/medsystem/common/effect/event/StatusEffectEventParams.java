package tnt.tarkovcraft.medsystem.common.effect.event;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;

public final class StatusEffectEventParams {

    public static final LootContextParam<DamageContext> DAMAGE_CONTEXT = key("damage_context");
    public static final LootContextParam<Float> DAMAGE_AMOUNT = key("damage_amount");
    public static final LootContextParam<Float> DAMAGE_AMOUNT_LIMB = key("damage_amount_limb");
    public static final LootContextParam<ItemStack> ITEM = key("item");

    private static <T> LootContextParam<T> key(String path) {
        return new LootContextParam<>(MedicalSystem.resource(path));
    }

    private StatusEffectEventParams() {}
}
