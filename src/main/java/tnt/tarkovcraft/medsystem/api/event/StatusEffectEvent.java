package tnt.tarkovcraft.medsystem.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import tnt.tarkovcraft.core.common.data.duration.TickValue;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.health.Limb;

import javax.annotation.Nullable;

public abstract class StatusEffectEvent extends Event {

    private final LivingEntity entity;
    private final StatusEffect statusEffect;
    @Nullable
    private final Limb limb;

    public StatusEffectEvent(LivingEntity entity, StatusEffect statusEffect, @Nullable Limb limb) {
        this.entity = entity;
        this.statusEffect = statusEffect;
        this.limb = limb;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public StatusEffect getStatusEffect() {
        return statusEffect;
    }

    @Nullable
    public Limb getBodyPart() {
        return limb;
    }

    public static class Schedule extends StatusEffectEvent {

        private int delay;
        private boolean cancelled;

        public Schedule(LivingEntity entity, StatusEffect statusEffect, @Nullable Limb limb, int delay) {
            super(entity, statusEffect, limb);
            this.delay = delay;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }

        public void setDelay(TickValue delay) {
            this.setDelay(delay.tickValue());
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }

    public static class Add extends StatusEffectEvent {

        private boolean cancelled;

        public Add(LivingEntity entity, StatusEffect statusEffect, @Nullable Limb limb) {
            super(entity, statusEffect, limb);
        }

        public void setCancelled() {
            this.cancelled = true;
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }

    public static class Remove extends StatusEffectEvent {

        public Remove(LivingEntity entity, StatusEffect statusEffect, @Nullable Limb limb) {
            super(entity, statusEffect, limb);
        }
    }
}
