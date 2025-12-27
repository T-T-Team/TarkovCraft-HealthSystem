package tnt.tarkovcraft.medsystem.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

public class PainCheckEvent extends Event {

    private final LivingEntity entity;
    private final HealthContainer container;
    private boolean inPain;

    public PainCheckEvent(LivingEntity entity, HealthContainer container, boolean inPain) {
        this.entity = entity;
        this.container = container;
        this.inPain = inPain;
    }

    public void setInPain(boolean inPain) {
        this.inPain = inPain;
    }

    public boolean isInPain() {
        return inPain;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public HealthContainer getContainer() {
        return container;
    }
}
