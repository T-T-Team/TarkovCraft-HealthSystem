package tnt.tarkovcraft.medsystem.api.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import tnt.tarkovcraft.medsystem.common.health.HealthContainer;

public class HitboxPiercingEvent extends Event {

    private final LivingEntity entity;
    private final DamageSource damageSource;
    private final HealthContainer container;
    private final Entity projectile;
    private final int originalPiercing;
    private int piercing;

    public HitboxPiercingEvent(LivingEntity entity, DamageSource damageSource, HealthContainer container, Entity projectile, int piercing) {
        this.entity = entity;
        this.damageSource = damageSource;
        this.container = container;
        this.projectile = projectile;
        this.originalPiercing = piercing;
        this.piercing = piercing;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public DamageSource getDamageSource() {
        return damageSource;
    }

    public HealthContainer getContainer() {
        return container;
    }

    public Entity getProjectile() {
        return projectile;
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
