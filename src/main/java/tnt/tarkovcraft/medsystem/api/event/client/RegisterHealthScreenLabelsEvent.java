package tnt.tarkovcraft.medsystem.api.event.client;

import net.neoforged.bus.api.Event;
import tnt.tarkovcraft.core.client.IconWithLabel;

import java.util.List;

public class RegisterHealthScreenLabelsEvent extends Event {

    private final List<IconWithLabel> labelProviders;

    public RegisterHealthScreenLabelsEvent(List<IconWithLabel> labelProviders) {
        this.labelProviders = labelProviders;
    }

    public List<IconWithLabel> getLabels() {
        return labelProviders;
    }
}
