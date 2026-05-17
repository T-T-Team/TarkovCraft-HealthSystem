package tnt.tarkovcraft.medsystem.api.event;

import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import tnt.tarkovcraft.medsystem.common.interaction.EntityInteraction;

import javax.annotation.Nonnull;
import java.util.List;

public class RegisterUnconsciousInteractionsEvent extends Event implements IModBusEvent {

    private final List<EntityInteraction> interactions;

    public RegisterUnconsciousInteractionsEvent(List<EntityInteraction> interactions) {
        this.interactions = interactions;
    }

    public void register(@Nonnull EntityInteraction interaction) {
        synchronized (this.interactions) {
            this.interactions.add(interaction);
        }
    }
}
