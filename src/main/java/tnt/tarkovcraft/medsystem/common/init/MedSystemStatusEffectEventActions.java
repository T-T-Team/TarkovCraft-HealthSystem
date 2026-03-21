package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.effect.event.action.*;

public final class MedSystemStatusEffectEventActions {

    public static final DeferredRegister<StatusEffectEventActionType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.STATUS_EFFECT_EVENT_ACTION, MedSystemConstants.MOD_ID);

    public static final Holder<StatusEffectEventActionType<?>> NONE = REGISTRY.register("none", key -> new StatusEffectEventActionType<>(key, NoStatusEffectEventAction.CODEC));
    public static final Holder<StatusEffectEventActionType<?>> ADD_STATUS_EFFECT = REGISTRY.register("add_status_effect", key -> new StatusEffectEventActionType<>(key, AddStatusEffectStatusEffectEventAction.CODEC));
    public static final Holder<StatusEffectEventActionType<?>> ADD_MOB_EFFECT = REGISTRY.register("add_mob_effect", key -> new StatusEffectEventActionType<>(key, AddMobEffectStatusEffectEventAction.CODEC));
    public static final Holder<StatusEffectEventActionType<?>> ADD_SHOCK = REGISTRY.register("add_shock", key -> new StatusEffectEventActionType<>(key, AddShockStatusEffectEvent.CODEC));
    public static final Holder<StatusEffectEventActionType<?>> COPY_INCOMING_EFFECTS = REGISTRY.register("copy_incoming_effects", key -> new StatusEffectEventActionType<>(key, CopyIncomingEffectsStatusEventAction.CODEC));

    public static final Holder<StatusEffectEventActionType<?>> WEIGHTED = REGISTRY.register("weighted", key -> new StatusEffectEventActionType<>(key, WeightedStatusEffectEventAction.CODEC));
}
