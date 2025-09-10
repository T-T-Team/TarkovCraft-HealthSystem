package tnt.tarkovcraft.medsystem.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import tnt.tarkovcraft.medsystem.common.health.DamageContext;

public class WoundStatusEffectApplyEvent extends Event {

    private final LivingEntity entity;
    private final DamageContext damageContext;
    private final float damageAmount;
    private int duration;

    public WoundStatusEffectApplyEvent(LivingEntity entity, DamageContext damageContext, float damageAmount, int duration) {
        this.entity = entity;
        this.damageContext = damageContext;
        this.damageAmount = damageAmount;
        this.duration = duration;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.duration = durationSeconds;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public DamageContext getDamageContext() {
        return damageContext;
    }

    public float getDamageAmount() {
        return damageAmount;
    }

    public int getDurationSeconds() {
        return duration;
    }

    public boolean shouldApplyEffect() {
        return this.duration > 0;
    }
}
