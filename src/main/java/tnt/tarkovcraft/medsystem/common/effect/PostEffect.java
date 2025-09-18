package tnt.tarkovcraft.medsystem.common.effect;

import tnt.tarkovcraft.core.common.data.duration.TickValue;

public record PostEffect(int delay, StatusEffect template) {

    public PostEffect(TickValue delay, StatusEffect template) {
        this(delay.tickValue(), template);
    }

    public PostEffect(StatusEffect template) {
        this(0, template);
    }

    public StatusEffect createInstance() {
        return this.template.copy();
    }
}
