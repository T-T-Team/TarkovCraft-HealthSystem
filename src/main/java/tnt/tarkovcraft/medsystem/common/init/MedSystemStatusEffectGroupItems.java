package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.effect.group.*;

public final class MedSystemStatusEffectGroupItems {

    public static final DeferredRegister<EffectGroupItemType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.EFFECT_GROUP_ITEM, MedicalSystem.MOD_ID);

    public static final Holder<EffectGroupItemType<?>> ATTRIBUTE = REGISTRY.register("attribute", key -> new EffectGroupItemType<>(key, AttributeModifierEffectGroupItem.CODEC));
    public static final Holder<EffectGroupItemType<?>> HEALTH = REGISTRY.register("health", key -> new EffectGroupItemType<>(key, HealthEffectGroupItem.CODEC));
    public static final Holder<EffectGroupItemType<?>> STATUS_EFFECT_REMOVING = REGISTRY.register("status_effect_removing", key -> new EffectGroupItemType<>(key, StatusEffectRemovingEffectGroupItem.CODEC));
}
