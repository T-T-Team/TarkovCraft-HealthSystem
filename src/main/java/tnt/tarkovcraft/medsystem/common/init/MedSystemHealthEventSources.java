package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.health_event.HealthEventTriggerSource;

public final class MedSystemHealthEventSources {

    public static final DeferredRegister<HealthEventTriggerSource> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.HEALTH_EVENT_TRIGGER_SOURCE, MedSystemConstants.MOD_ID);

    public static final Holder<HealthEventTriggerSource> UPDATE = register("update");
    public static final Holder<HealthEventTriggerSource> INCOMING_DAMAGE = register("incoming_damage");
    public static final Holder<HealthEventTriggerSource> INCOMING_DAMAGE_GLOBAL = register("incoming_damage_global");
    public static final Holder<HealthEventTriggerSource> CONSUME = register("consume");

    private static Holder<HealthEventTriggerSource> register(String id) {
        return REGISTRY.register(id, HealthEventTriggerSource::new);
    }
}
