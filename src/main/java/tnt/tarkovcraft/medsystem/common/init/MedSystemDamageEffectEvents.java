package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.MedicalSystem;
import tnt.tarkovcraft.medsystem.common.damage_effect.event.*;

public final class MedSystemDamageEffectEvents {

    public static final DeferredRegister<DamageEffectEventType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.DAMAGE_EFFECT_EVENT, MedicalSystem.MOD_ID);

    public static final Holder<DamageEffectEventType<?>> NONE = REGISTRY.register("none", key -> new DamageEffectEventType<>(key, NoDamageEffectEvent.CODEC));
    public static final Holder<DamageEffectEventType<?>> ADD_STATUS_EFFECT = REGISTRY.register("add_status_effect", key -> new DamageEffectEventType<>(key, AddStatusEffectDamageEffectEvent.CODEC));
    public static final Holder<DamageEffectEventType<?>> ADD_MOB_EFFECT = REGISTRY.register("add_mob_effect", key -> new DamageEffectEventType<>(key, AddMobEffectDamageEffectEvent.CODEC));
    public static final Holder<DamageEffectEventType<?>> COPY_INCOMING_EFFECTS = REGISTRY.register("copy_incoming_effects", key -> new DamageEffectEventType<>(key, CopyIncomingEffectsDamageEvent.CODEC));

    public static final Holder<DamageEffectEventType<?>> WEIGHTED = REGISTRY.register("weighted", key -> new DamageEffectEventType<>(key, WeightedDamageEffectEvent.CODEC));
}
