package tnt.tarkovcraft.medsystem.api.event;

import net.neoforged.bus.api.Event;
import tnt.tarkovcraft.medsystem.common.damage.DamageResolver;

import java.util.function.Consumer;

public final class AddBuiltInDamageResolversEvent extends Event {

    private final Consumer<DamageResolver> registration;

    public AddBuiltInDamageResolversEvent(Consumer<DamageResolver> registration) {
        this.registration = registration;
    }

    public void register(DamageResolver resolver) {
        this.registration.accept(resolver);
    }
}
