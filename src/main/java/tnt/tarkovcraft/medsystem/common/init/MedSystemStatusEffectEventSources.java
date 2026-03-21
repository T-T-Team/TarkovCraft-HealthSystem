package tnt.tarkovcraft.medsystem.common.init;

import net.minecraft.core.Holder;
import net.neoforged.neoforge.registries.DeferredRegister;
import tnt.tarkovcraft.medsystem.api.MedSystemConstants;
import tnt.tarkovcraft.medsystem.common.effect.event.StatusEffectEventSource;

public final class MedSystemStatusEffectEventSources {

    public static final DeferredRegister<StatusEffectEventSource> REGISTRY = DeferredRegister.create(MedSystemRegistries.Keys.STATUS_EFFECT_EVENT_SOURCE, MedSystemConstants.MOD_ID);

    public static final Holder<StatusEffectEventSource> UPDATE = register("tickProgram");
    public static final Holder<StatusEffectEventSource> INCOMING_DAMAGE = register("incoming_damage");
    public static final Holder<StatusEffectEventSource> INCOMING_DAMAGE_GLOBAL = register("incoming_damage_global");
    // TODO implement
    public static final Holder<StatusEffectEventSource> CONSUME = register("consume");

    private static Holder<StatusEffectEventSource> register(String id) {
        return REGISTRY.register(id, StatusEffectEventSource::new);
    }
}
