package tnt.tarkovcraft.medsystem.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import tnt.tarkovcraft.medsystem.common.effect.StatusEffect;
import tnt.tarkovcraft.medsystem.common.health.BodyPart;

import javax.annotation.Nullable;

public abstract class StatusEffectEvent extends Event {

    private final LivingEntity entity;
    private final StatusEffect statusEffect;
    @Nullable
    private final BodyPart bodyPart;

    public StatusEffectEvent(LivingEntity entity, StatusEffect statusEffect, @Nullable BodyPart bodyPart) {
        this.entity = entity;
        this.statusEffect = statusEffect;
        this.bodyPart = bodyPart;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public StatusEffect getStatusEffect() {
        return statusEffect;
    }

    @Nullable
    public BodyPart getBodyPart() {
        return bodyPart;
    }

    public static class Add extends StatusEffectEvent {

        private boolean cancelled;

        public Add(LivingEntity entity, StatusEffect statusEffect, @org.jetbrains.annotations.Nullable BodyPart bodyPart) {
            super(entity, statusEffect, bodyPart);
        }

        public void setCancelled() {
            this.cancelled = true;
        }

        public boolean isCancelled() {
            return cancelled;
        }
    }

    public static class Remove extends StatusEffectEvent {

        public Remove(LivingEntity entity, StatusEffect statusEffect, @org.jetbrains.annotations.Nullable BodyPart bodyPart) {
            super(entity, statusEffect, bodyPart);
        }
    }
}
