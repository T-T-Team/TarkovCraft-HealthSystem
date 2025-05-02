package tnt.tarkovcraft.medsystem.common.health;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import tnt.tarkovcraft.core.util.context.ContextImpl;
import tnt.tarkovcraft.core.util.context.WritableContext;
import tnt.tarkovcraft.medsystem.common.health.math.DamageDistributor;
import tnt.tarkovcraft.medsystem.common.health.math.EvenDamageDistributor;
import tnt.tarkovcraft.medsystem.common.health.math.HitCalculator;

import java.util.List;

public final class DamageContext {

    private final LivingEntity entity;
    private final DamageSource source;
    private final long id;
    private List<HitResult> hits;
    private List<EquipmentSlot> affectedSlots;
    private HitCalculator hitCalculator;
    private DamageDistributor damageDistributor;
    private final WritableContext data = ContextImpl.builder().build();

    public DamageContext(LivingEntity entity, DamageSource source) {
        this.entity = entity;
        this.source = source;
        this.id = entity.level().getGameTime();
    }

    public HitCalculator getHitCalculator() {
        return hitCalculator;
    }

    public void setHitCalculator(HitCalculator hitCalculator) {
        this.hitCalculator = hitCalculator;
    }

    public void setHits(List<HitResult> hits) {
        this.hits = hits;
    }

    public void setAffectedSlots(List<EquipmentSlot> affectedSlots) {
        this.affectedSlots = affectedSlots;
    }

    public void setDamageDistributor(DamageDistributor damageDistributor) {
        this.damageDistributor = damageDistributor;
    }

    public long getId() {
        return id;
    }

    public LivingEntity getEntity() {
        return entity;
    }

    public DamageSource getSource() {
        return source;
    }

    public List<HitResult> getHits() {
        return hits;
    }

    public List<EquipmentSlot> getAffectedSlots() {
        return affectedSlots;
    }

    public DamageDistributor getDamageDistributor(HealthContainer container) {
        DamageDistributor original = this.damageDistributor != null ? this.damageDistributor : EvenDamageDistributor.INSTANCE;
        DamageDistributor custom = this.hitCalculator.getCustomDamageDistributor(this.entity, this.source, container, original);
        return custom != null ? custom : original;
    }

    public WritableContext getData() {
        return data;
    }
}
