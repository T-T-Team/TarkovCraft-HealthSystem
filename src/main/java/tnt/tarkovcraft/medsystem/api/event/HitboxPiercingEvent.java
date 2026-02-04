package tnt.tarkovcraft.medsystem.api.event;

import net.neoforged.bus.api.Event;
import tnt.tarkovcraft.medsystem.common.health.calc.HitCalculationContext;

@Deprecated
public class HitboxPiercingEvent extends Event {

    private final HitCalculationContext context;
    private final int originalPiercing;
    private int piercing;

    public HitboxPiercingEvent(HitCalculationContext context, int piercing) {
        this.context = context;
        this.originalPiercing = piercing;
        this.piercing = piercing;
    }

    public HitCalculationContext getContext() {
        return context;
    }

    public int getOriginalPiercing() {
        return originalPiercing;
    }

    public int getPiercing() {
        return piercing;
    }

    public void setPiercing(int piercing) {
        this.piercing = piercing;
    }
}
