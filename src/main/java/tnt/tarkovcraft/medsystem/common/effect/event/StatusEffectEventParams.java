package tnt.tarkovcraft.medsystem.common.effect.event;

import net.minecraft.util.context.ContextKey;
import net.minecraft.world.item.ItemStack;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;

public final class StatusEffectEventParams {

    public static final ContextKey<DamageContext> DAMAGE_CONTEXT = key("damage_context");
    public static final ContextKey<Float> DAMAGE_AMOUNT = key("damage_amount");
    public static final ContextKey<Float> DAMAGE_AMOUNT_LIMB = key("damage_amount_limb");
    public static final ContextKey<Integer> LIMBS_LOST = key("limbs_lost");
    public static final ContextKey<ItemStack> ITEM = key("item");

    private static <T> ContextKey<T> key(String path) {
        return new ContextKey<>(MedicalSystem.createIdentifier(path));
    }

    private StatusEffectEventParams() {}
}
