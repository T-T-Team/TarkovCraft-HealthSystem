package tnt.tarkovcraft.medsystem.api.event.client;

import net.neoforged.bus.api.Event;
import tnt.tarkovcraft.medsystem.client.screen.HealthScreen;

import java.util.List;

public class RegisterHealthScreenLabelsEvent extends Event {

    private final List<HealthScreen.LabelProvider> labelProviders;

    public RegisterHealthScreenLabelsEvent(List<HealthScreen.LabelProvider> labelProviders) {
        this.labelProviders = labelProviders;
    }

    public List<HealthScreen.LabelProvider> getLabels() {
        return labelProviders;
    }
}
