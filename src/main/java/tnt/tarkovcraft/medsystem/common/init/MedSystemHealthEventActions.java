package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.health_event.action.*;

public final class MedSystemHealthEventActions {

    public static final DeferredRegister<HealthEventActionType<?>> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.HEALTH_EVENT_ACTION, MedSystemConstants.MOD_ID);

    public static final Holder<HealthEventActionType<?>> NONE = REGISTRY.register("none", key -> new HealthEventActionType<>(key, NoEventAction.CODEC));
    public static final Holder<HealthEventActionType<?>> ADD_STATUS_EFFECT = REGISTRY.register("add_status_effect", key -> new HealthEventActionType<>(key, AddStatusEffectEventAction.CODEC));
    public static final Holder<HealthEventActionType<?>> ADD_MOB_EFFECT = REGISTRY.register("add_mob_effect", key -> new HealthEventActionType<>(key, AddMobEffectEventAction.CODEC));
    public static final Holder<HealthEventActionType<?>> ADD_SHOCK = REGISTRY.register("add_shock", key -> new HealthEventActionType<>(key, AddShockEventAction.CODEC));
    public static final Holder<HealthEventActionType<?>> COPY_INCOMING_EFFECTS = REGISTRY.register("copy_incoming_effects", key -> new HealthEventActionType<>(key, CopyIncomingEffectsEventAction.CODEC));

    public static final Holder<HealthEventActionType<?>> WEIGHTED = REGISTRY.register("weighted", key -> new HealthEventActionType<>(key, WeightedEventAction.CODEC));
}
