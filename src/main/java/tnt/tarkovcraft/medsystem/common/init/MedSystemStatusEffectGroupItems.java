package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.common.effect.group.*;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;

public final class MedSystemStatusEffectGroupItems {

    public static final DeferredRegister<EffectGroupItemType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.EFFECT_GROUP_ITEM, MedSystemConstants.MOD_ID);

    public static final Holder<EffectGroupItemType<?>> ATTRIBUTE = REGISTRY.register("attribute", key -> new EffectGroupItemType<>(key, AttributeModifierEffectGroupItem.CODEC));
    public static final Holder<EffectGroupItemType<?>> HEALTH = REGISTRY.register("health", key -> new EffectGroupItemType<>(key, HealthEffectGroupItem.CODEC));
    public static final Holder<EffectGroupItemType<?>> STATUS_EFFECT_REMOVING = REGISTRY.register("status_effect_removing", key -> new EffectGroupItemType<>(key, StatusEffectRemovingEffectGroupItem.CODEC));
    public static final Holder<EffectGroupItemType<?>> DEAD_LIMB_RECOVERY = REGISTRY.register("dead_limb_recovery", key -> new EffectGroupItemType<>(key, DeadLimbRecoveryEffectGroupItem.CODEC));
    public static final Holder<EffectGroupItemType<?>> BLOOD_RECOVERY = REGISTRY.register("blood_recovery", key  -> new EffectGroupItemType<>(key, BloodRecoveryEffectGroupItem.CODEC));
    public static final Holder<EffectGroupItemType<?>> MOB_EFFECT = REGISTRY.register("mob_effect", key -> new EffectGroupItemType<>(key, MobEffectGroupItem.CODEC));
}
